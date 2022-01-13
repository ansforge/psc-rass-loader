/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.controller;

import fr.ans.psc.pscload.component.DuplicateKeyException;
import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.state.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Optional;

/**
 * The Class ProcessController.
 */
@Slf4j
@RestController
public class TestingController {

	/** The files directory. */
	@Value("${files.directory}")
	private String filesDirectory;

	@Value("${extract.download.url}")
	private String extractDownloadUrl;

	@Autowired
	private CustomMetrics customMetrics;

	/** The registry. */
	private final ProcessRegistry registry;

	/**
	 * The Enum States.
	 */
	private enum States {
		SUBMITTED("Submitted", Submitted.class),
		READY_TO_EXTRACT("ReadyToExtract", ReadyToExtract.class),
		READY_TO_COMPUTE_DIFF("ReadyToComputeDiff", ReadyToComputeDiff.class),
		DIFF_COMPUTED("DiffComputed", DiffComputed.class);

		private Class<? extends ProcessState> clazz;
		private String classname;

		private States(String classname, Class<? extends ProcessState> clazz) {
			this.clazz = clazz;
			this.classname = classname.toLowerCase();
		}
	}

	/**
	 * Instantiates a new Testing controller.
	 *
	 * @param registry the registry
	 */
	public TestingController(ProcessRegistry registry) {
		super();
		this.registry = registry;
	}
	
	/**
	 * Create a new process with no state.
	 *
	 * @return the response entity with the new process id.
	 */
	@PostMapping(value = "/test/process/new")
	public ResponseEntity<String> createProcess(@RequestParam Optional<String> downloadedfilename,
			@RequestParam Optional<String> extractedfilename) {
		String id = Integer.toString(registry.nextId());
		LoadProcess process = new LoadProcess();
		downloadedfilename.ifPresent(filename -> process.setDownloadedFilename(filesDirectory + File.separator + filename ));
		extractedfilename.ifPresent(filename -> process.setExtractedFilename(filesDirectory + File.separator + filename));
		try {
			registry.register(id, process);
		} catch (DuplicateKeyException e) {
			return new ResponseEntity<>(String.format("Duplicate Process id %s", id), HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(id, HttpStatus.OK);
	}

	/**
	 * Unregister a process with id.
	 *
	 * @return OK if success.
	 */
	@GetMapping(value = "/test/process/unregister")
	public ResponseEntity<Void> unregisterProcess(@RequestParam String id) {
		registry.unregister(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * set new Status if authorized with process id.
	 *
	 * @return OK if success or 404 if state is not found.
	 */
	@PutMapping(value = "/test/process/setstate")
	public ResponseEntity<Void> setState(@RequestParam String id, @RequestParam String state) {
		ProcessState processState = null;
		try {
			if (States.SUBMITTED.classname.equals(state)) {
				processState = new Submitted(extractDownloadUrl, filesDirectory);
			} else if (States.READY_TO_EXTRACT.classname.equals(state)) {
				processState = new ReadyToExtract();
			} else if (States.READY_TO_COMPUTE_DIFF.classname.equals(state)) {
				processState = new ReadyToComputeDiff(customMetrics);
			} else if (States.DIFF_COMPUTED.classname.equals(state)){
				processState = new DiffComputed(customMetrics);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			// normally not reachable
			log.error("error when instantiate class",e);
		}
		registry.getProcessById(id).setState(processState);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * run the task of the current state.
	 *
	 * @return OK if success or 500 if state or process is null.
	 */
	@PutMapping(value = "/test/process/runtask")
	public ResponseEntity<Void> runtask(@RequestParam String id) {
		LoadProcess process = registry.getProcessById(id);
		if (process != null) {
			ProcessState state = process.getState();
			if (state != null) {
				process.nextStep();
			}else {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}else {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
