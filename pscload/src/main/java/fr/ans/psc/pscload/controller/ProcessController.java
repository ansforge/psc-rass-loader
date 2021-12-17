/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.controller;

import java.util.concurrent.ForkJoinPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.ChangesApplied;
import fr.ans.psc.pscload.state.DiffComputed;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ProcessController.
 */
@Slf4j
@RestController
public class ProcessController {

	@Autowired
	private CustomMetrics customMetrics;
	
	@Autowired
	private final ProcessRegistry registry;

	/**
	 * Instantiates a new process controller.
	 *
	 * @param registry the registry
	 */
	public ProcessController(ProcessRegistry registry) {
		super();
		this.registry = registry;
	}

	/**
	 * Continue process.
	 *
	 * @return the deferred result
	 */
	@PostMapping(value = "/process/continue")
	public DeferredResult<ResponseEntity<Void>> continueProcess() {
		LoadProcess process = registry.getCurrentProcess();

		DeferredResult<ResponseEntity<Void>> response = new DeferredResult<>();
		if (process.getState().getClass().equals(DiffComputed.class)) {
			// launch process in a separate thread
			ForkJoinPool.commonPool().submit(() -> {
				try {
					// upload changes
					customMetrics.resetSizeMetrics();
					process.runtask();
					process.setState(new ChangesApplied());
					// Build message with failed requests
					String message = buildMessageBody(process);
					customMetrics.setStageMetric(40, message);
					// Step 5 : call pscload
					process.runtask();
					registry.unregister(process.getId());
					customMetrics.setStageMetric(0);
				} catch (LoadProcessException e) {
					log.error("error when uploading changes", e);
				}
				ResponseEntity<Void> result = new ResponseEntity<Void>(HttpStatus.ACCEPTED);
				response.setResult(result);
			});
			// Response OK
			return response;
		}
		// Conflict if process is not in the expected state.
		ResponseEntity<Void> result = new ResponseEntity<Void>(HttpStatus.CONFLICT);
		response.setResult(result);
		return response;
	}

	private String buildMessageBody(LoadProcess process) {
		StringBuilder message = new StringBuilder();
		message.append("Créations PS en échec :");
		message.append(System.lineSeparator());
		process.getPsToCreate().values().stream().forEach(ps -> {
			int status = ps.getReturnStatus();
			String nationalId = ps.getNationalId();
			message.append(System.lineSeparator());
			message.append("PS : " + nationalId);
			message.append("status code :" + status);
			message.append(System.lineSeparator());
		});
		message.append("Suppressions PS en échec :");
		message.append(System.lineSeparator());
		process.getPsToDelete().values().stream().forEach(ps -> {
			int status = ps.getReturnStatus();
			String nationalId = ps.getNationalId();
			message.append("PS : " + nationalId);
			message.append("status code :" + status);
			message.append(System.lineSeparator());
		});
		message.append("Modifications PS en échec :");
		message.append(System.lineSeparator());
		process.getPsToUpdate().values().stream().forEach(v -> {
			int status = v.rightValue().getReturnStatus();
			String nationalId = v.rightValue().getNationalId();
			message.append("PS : " + nationalId);
			message.append("status code :" + status);
			message.append(System.lineSeparator());
		});
		message.append("Créations Structure en échec :");
		message.append(System.lineSeparator());
		process.getStructureToCreate().values().stream().forEach(structure -> {
			int status = structure.getReturnStatus();
			String nationalId = structure.getStructureTechnicalId();
			message.append(System.lineSeparator());
			message.append("Structure : " + nationalId);
			message.append("status code :" + status);
			message.append(System.lineSeparator());
		});
		message.append("Suppressions Structure en échec :");
		message.append(System.lineSeparator());
		process.getStructureToDelete().values().stream().forEach(structure -> {
			int status = structure.getReturnStatus();
			String nationalId = structure.getStructureTechnicalId();
			message.append("Structure : " + nationalId);
			message.append("status code :" + status);
			message.append(System.lineSeparator());
		});
		message.append("Modifications Structure en échec :");
		message.append(System.lineSeparator());
		process.getStructureToUpdate().values().stream().forEach(v -> {
			int status = v.rightValue().getReturnStatus();
			String nationalId = v.rightValue().getStructureTechnicalId();
			message.append("Structure : " + nationalId);
			message.append("status code :" + status);
			message.append(System.lineSeparator());
		});
		message.append("Si certaines modifications n'ont pas été appliquées, ");
		message.append("vérifiez la plateforme et tentez de relancer le process à partir du endpoint \"resume\"");
		return message.toString();
	}

	/**
	 * Abort process.
	 *
	 * @return the response entity
	 */
	@PostMapping(value = "/process/abort")
	public ResponseEntity<Void> abortProcess() {
		// TODO check il clear is a better way to abort ?
		registry.unregister(registry.getCurrentProcess().getId());

		return null;
	}

	/**
	 * Resume process.
	 *
	 * @return the deferred result
	 */
	@PostMapping(value = "/process/resume")
	public DeferredResult<ResponseEntity<Void>> resumeProcess() {
		// We can call continue process because it contains the updated maps to apply.
		return continueProcess();
	}
}
