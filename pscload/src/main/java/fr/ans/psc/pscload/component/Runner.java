/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.ChangesApplied;
import fr.ans.psc.pscload.state.DiffComputed;
import fr.ans.psc.pscload.state.Idle;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.ReadyToComputeDiff;
import fr.ans.psc.pscload.state.ReadyToExtract;
import fr.ans.psc.pscload.state.UploadingChanges;
import fr.ans.psc.pscload.state.exception.ChangesApplicationException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class Scheduler.
 */
@Slf4j
@Component
public class Runner {

	@Autowired
	private ProcessRegistry processRegistry;

	@Autowired
	private CustomMetrics customMetrics;

	@Value("${enable.scheduler:true}")
	private boolean enabled;

	@Value("${extract.download.url}")
	private String extractDownloadUrl;

	@Value("${cert.path}")
	private String certfile;

	@Value("${key.path}")
	private String keyfile;

	@Value("${ca.path}")
	private String cafile;

	@Value("${keystore.password:mysecret}")
	private String kspwd;

	@Value("${files.directory}")
	private String filesDirectory;

	@Value("${use.x509.auth:true}")
	private boolean useX509Auth;

	@Value("${api.base.url}")
	private String apiBaseUrl;

	@Value("${deactivation.excluded.profession.codes:}")
	private String[] excludedProfessions;

	@Value("${pscextract.base.url}")
	private String pscextractBaseUrl;


	/**
	 * Run.
	 *
	 * @throws DuplicateKeyException    the duplicate key exception
	 */
	@Scheduled(cron  = "${schedule.cron.expression}", zone = "${schedule.cron.timeZone}")
	public void runScheduler() throws DuplicateKeyException {
		if (enabled) {
			if (processRegistry.isEmpty()) {
				String id = Integer.toString(processRegistry.nextId());
				ProcessState idle;
				if (useX509Auth) {
					idle = new Idle(keyfile, certfile, cafile, kspwd, extractDownloadUrl, filesDirectory);
				} else {
					idle = new Idle(extractDownloadUrl, filesDirectory);
				}
				LoadProcess process = new LoadProcess(idle);
				processRegistry.register(id, process);
				try {
					// Step 1 : Download
					process.nextStep();
					process.setState(new ReadyToExtract());
					customMetrics.setStageMetric(10);
					// Step 2 : Extract
					process.nextStep();
					process.setState(new ReadyToComputeDiff());
					customMetrics.setStageMetric(30);
					// Step 4 : Load maps and compute diff
					process.nextStep();
					process.setState(new DiffComputed(customMetrics));
					customMetrics.setStageMetric(50);
					// Step 3 : publish metrics
					process.nextStep();
					// End of scheduled steps
				} catch (LoadProcessException e) {
					log.error("Error when loading RASS data", e);
					processRegistry.unregister(id);
				}
			} else {
				log.warn("A process is already running !");
			}
		}
	}

	public void runContinue(LoadProcess process) {
		try {
			// upload changes
			process.setState(new UploadingChanges(excludedProfessions, apiBaseUrl));
			customMetrics.resetSizeMetrics();
			customMetrics.setStageMetric(60);
			process.nextStep();
			process.setState(new ChangesApplied(customMetrics, pscextractBaseUrl));
			// Step 5 : call pscload
			process.nextStep();
			processRegistry.unregister(process.getId());
			customMetrics.setStageMetric(0);
		} catch (LoadProcessException e) {
			if (e.getClass().equals(ChangesApplicationException.class)) {
				customMetrics.setStageMetric(60, "warning" + e.getMessage());
				processRegistry.unregister(process.getId());
			}

			// TODO
			// se mettre dans un état pending, en attente d'un resume ?
			log.error("error when uploading changes", e);
		}
	}
}
