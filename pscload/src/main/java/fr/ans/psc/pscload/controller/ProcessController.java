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

@Slf4j
@RestController
public class ProcessController {
	
	@Autowired
	private CustomMetrics customMetrics;
	private final ProcessRegistry registry;
	
	public ProcessController(ProcessRegistry registry) {
		super();
		this.registry = registry;
	}


	@PostMapping(value = "/process/continue")
    public DeferredResult<ResponseEntity<Void>> continueProcess() {
		LoadProcess process = registry.getCurrentProcess();
		DeferredResult<ResponseEntity<Void>> response = new DeferredResult<>();
		if (process.getState().getClass().equals(DiffComputed.class)) {
			// launch process in a separate thread
			ForkJoinPool.commonPool().submit(() -> {
			try {
				//upload changes
				customMetrics.resetSizeMetrics();
				process.runtask();
				process.setState(new ChangesApplied());
				customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE).set(40);
				// Step 5 : call pscload
				process.runtask();
				registry.unregister(process.getId());
				customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE).set(0);
			} catch (LoadProcessException e) {				
				log.error("error when uploading changes", e);		
			}
			ResponseEntity<Void> result = new ResponseEntity<Void>(HttpStatus.ACCEPTED);
			response.setResult(result);
		    });
			// Response OK
			return response;
		}
		//Conflict if process is not in the expected state.
		ResponseEntity<Void> result = new ResponseEntity<Void>(HttpStatus.CONFLICT);
		response.setResult(result);
		return response;
    }

	
	@PostMapping(value = "/process/abort")
    public ResponseEntity<Void> abortProcess() {
        return null;
    }

}
