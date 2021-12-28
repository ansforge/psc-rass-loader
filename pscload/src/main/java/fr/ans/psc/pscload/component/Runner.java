/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.component;

import fr.ans.psc.pscload.service.EmailNature;
import fr.ans.psc.pscload.state.exception.ExtractTriggeringException;
import fr.ans.psc.pscload.state.exception.UploadException;
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
import fr.ans.psc.pscload.state.exception.SerFileGenerationException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Date;

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

	@Value("${process.expiration.delay}")
	private Long expirationDelay;


	/**
	 * Run.
	 *
	 * @throws DuplicateKeyException    the duplicate key exception
	 */
	@Scheduled(cron  = "${scheduler.cron}")
	public void runScheduler() throws DuplicateKeyException {
		if (enabled) {
			if (processRegistry.isEmpty() || isProcessExpired()) {
				// clear registry if latest is expired
				processRegistry.clear();

				// register new process with Idle state
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
					customMetrics.setStageMetric(
							customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE).get(),
							EmailNature.INTERRUPTED_PROCESS);
					processRegistry.unregister(id);
				}
			} else {
				log.warn("A process is already running !");
			}
		}
	}

	private boolean isProcessExpired() {
		if (processRegistry.isEmpty()) { return true; }
		Date lastProcessDate = new Date(processRegistry.getCurrentProcess().getTimestamp());
		Date now = new Date();
		return now.after(
				Date.from(lastProcessDate.toInstant().plus(Duration.ofHours(expirationDelay))));
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
			// error during uploading
			if (e.getClass().equals(UploadException.class)) {
				log.error("error when uploading changes", e);
				customMetrics.setStageMetric(60, EmailNature.UPLOAD_REST_INTERRUPTION);
				// error during serialization/deserialization
			} else if (e.getClass().equals(SerFileGenerationException.class)) {
				customMetrics.setStageMetric(60, EmailNature.SERIALIZATION_FAILURE);
//				processRegistry.unregister(process.getId());
				// error when triggering extract
			} else if (e.getClass().equals(ExtractTriggeringException.class)) {
				log.warn("Error when triggering pscextract", e);
				customMetrics.setStageMetric(70, EmailNature.TRIGGER_EXTRACT_FAILED);
				processRegistry.unregister(process.getId());
			}
		}
	}
}
