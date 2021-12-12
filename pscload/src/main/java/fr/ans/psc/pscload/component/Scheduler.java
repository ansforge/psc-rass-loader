package fr.ans.psc.pscload.component;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.MapDifference;

import fr.ans.psc.pscload.PscloadServletContextListener;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.ChangesApplied;
import fr.ans.psc.pscload.state.DiffComputed;
import fr.ans.psc.pscload.state.FileDownloaded;
import fr.ans.psc.pscload.state.FileExtracted;
import fr.ans.psc.pscload.state.Idle;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Scheduler.
 */
@Slf4j
@Component
public class Scheduler {

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
	
    @Value("${api.base.url}")
    private String apiBaseUrl;

    @Value("${deactivation.excluded.profession.codes:}")
    private String[] excludedProfessions;

	/**
	 * Run the process
	 */
	@Scheduled(fixedDelayString = "${schedule.rate.ms}")
	public void run() throws GeneralSecurityException, IOException {
		if (enabled) {
			if (processRegistry.isEmpty()) {
				LoadProcess process = new LoadProcess(new Idle(keyfile, certfile, cafile));
				String id = Integer.toString(processRegistry.nextId());
				processRegistry.register(id, process);
				try {
					// Step 1 : Download
					process.runtask();
					process.setState(new FileDownloaded());
					customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE).set(10);
					// Step 2 : Extract
					process.runtask();
					process.setState(new FileExtracted());
					customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE).set(20);
					// Step 3 : Load maps and compute diff
					process.runtask();
					publishPsMetrics();
					publishStructureMetrics();
					process.setState(new DiffComputed(excludedProfessions, apiBaseUrl));
					customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE).set(30);
					//End of scheduled steps
				} catch (LoadProcessException e) {
					// TODO log
					processRegistry.unregister(id);
				}
			}else {
				//TODO  ajouter un log et ne rien faire car un process est déjà en cours.
			}
		}
	}
	
	
	private void publishPsMetrics() {
		MapDifference<String, Professionnel> psDiff = processRegistry.getCurrentProcess().getPsMap();
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_ADELI_DELETE_SIZE).set(
	            Math.toIntExact(psDiff.entriesOnlyOnLeft().values().stream().filter(ps -> CustomMetrics.ID_TYPE.ADELI.value.equals(ps.getIdType())).count()));
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_ADELI_CREATE_SIZE).set(
	            Math.toIntExact(psDiff.entriesOnlyOnRight().values().stream().filter(ps -> CustomMetrics.ID_TYPE.ADELI.value.equals(ps.getIdType())).count()));
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_ADELI_UPDATE_SIZE).set(
	            Math.toIntExact(psDiff.entriesDiffering().values().stream().filter(ps -> CustomMetrics.ID_TYPE.ADELI.value.equals(ps.leftValue().getIdType())).count()));
	
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_FINESS_DELETE_SIZE).set(
	            Math.toIntExact(psDiff.entriesOnlyOnLeft().values().stream().filter(ps -> CustomMetrics.ID_TYPE.FINESS.value.equals(ps.getIdType())).count()));
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_FINESS_CREATE_SIZE).set(
	            Math.toIntExact(psDiff.entriesOnlyOnRight().values().stream().filter(ps -> CustomMetrics.ID_TYPE.FINESS.value.equals(ps.getIdType())).count()));
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_FINESS_UPDATE_SIZE).set(
	            Math.toIntExact(psDiff.entriesDiffering().values().stream().filter(ps -> CustomMetrics.ID_TYPE.FINESS.value.equals(ps.leftValue().getIdType())).count()));
	
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_SIRET_DELETE_SIZE).set(
	            Math.toIntExact(psDiff.entriesOnlyOnLeft().values().stream().filter(ps -> CustomMetrics.ID_TYPE.SIRET.value.equals(ps.getIdType())).count()));
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_SIRET_CREATE_SIZE).set(
	            Math.toIntExact(psDiff.entriesOnlyOnRight().values().stream().filter(ps -> CustomMetrics.ID_TYPE.SIRET.value.equals(ps.getIdType())).count()));
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_SIRET_UPDATE_SIZE).set(
	            Math.toIntExact(psDiff.entriesDiffering().values().stream().filter(ps -> CustomMetrics.ID_TYPE.SIRET.value.equals(ps.leftValue().getIdType())).count()));
	
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_RPPS_DELETE_SIZE).set(
	            Math.toIntExact(psDiff.entriesOnlyOnLeft().values().stream().filter(ps -> CustomMetrics.ID_TYPE.RPPS.value.equals(ps.getIdType())).count()));
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_RPPS_CREATE_SIZE).set(
	            Math.toIntExact(psDiff.entriesOnlyOnRight().values().stream().filter(ps -> CustomMetrics.ID_TYPE.RPPS.value.equals(ps.getIdType())).count()));
	    customMetrics.getPsSizeGauges().get(CustomMetrics.PsCustomMetric.PS_RPPS_UPDATE_SIZE).set(
	            Math.toIntExact(psDiff.entriesDiffering().values().stream().filter(ps -> CustomMetrics.ID_TYPE.RPPS.value.equals(ps.leftValue().getIdType())).count()));
	
	
	}
	private void publishStructureMetrics() {
		MapDifference<String, Structure> structureDiff = processRegistry.getCurrentProcess().getStructureMap();
	    customMetrics.getAppStructureSizeGauges().get(CustomMetrics.StructureCustomMetric.STRUCTURE_DELETE_SIZE).set(structureDiff.entriesOnlyOnLeft().size());
	    customMetrics.getAppStructureSizeGauges().get(CustomMetrics.StructureCustomMetric.STRUCTURE_CREATE_SIZE).set(structureDiff.entriesOnlyOnRight().size());
	    customMetrics.getAppStructureSizeGauges().get(CustomMetrics.StructureCustomMetric.STRUCTURE_UPDATE_SIZE).set(structureDiff.entriesDiffering().size());

	}
}
