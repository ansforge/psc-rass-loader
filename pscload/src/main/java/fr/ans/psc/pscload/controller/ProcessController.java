/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.component.Runner;
import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.ProcessInfo;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.DiffComputed;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ProcessController.
 */
@Slf4j
@RestController
public class ProcessController {

    @Value("${files.directory}")
    private String filesDirectory;

    @Autowired
    private Runner runner;

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
        if (process != null) {
            if (process.getState().getClass().equals(DiffComputed.class)) {
                // launch process in a separate thread
                ForkJoinPool.commonPool().submit(() -> {
                    runner.runContinue(process);
                    ResponseEntity<Void> result = new ResponseEntity<Void>(HttpStatus.ACCEPTED);
                    response.setResult(result);
                });
                // Response OK
                response.onCompletion(() -> log.info("Processing complete"));
                return response;
            }
            // Conflict if process is not in the expected state.
            ResponseEntity<Void> result = new ResponseEntity<Void>(HttpStatus.CONFLICT);
            response.setResult(result);
        } else {
            response.setResult(new ResponseEntity<Void>(HttpStatus.TOO_EARLY));
        }
        return response;
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

    /**
     * Abort process.
     *
     * @return the response entity
     */
    @PostMapping(value = "/process/abort")
    public ResponseEntity<Void> abortProcess() {
        // TODO check if clear is a better way to abort ?
        registry.unregister(registry.getCurrentProcess().getId());

        return null;
    }

    /**
     * info on  process.
     *
     * @return the result
     */
    @GetMapping(value = "/process/info")
    public ResponseEntity<List<ProcessInfo>> processInfo() {
        ProcessInfo infos = new ProcessInfo();
        List<LoadProcess> processes = registry.list();
        List<ProcessInfo> processesInfos = new ArrayList<>();
        for (LoadProcess process : processes) {
            infos.setProcessId(process.getId());
            DateFormat df = new SimpleDateFormat();
            infos.setCreatedOn(df.format(new Date(process.getTimestamp())));
            infos.setState(process.getState().getClass().getSimpleName());
            if (process.getState().getClass().equals(DiffComputed.class)) {
                infos.setPsToCreate(process.getPsToCreate().size());
                infos.setPsToUpdate(process.getPsToUpdate().size());
                infos.setPsToDelete(process.getPsToDelete().size());
                infos.setStructureToCreate(process.getStructureToCreate().size());
                infos.setStructureToUpdate(process.getStructureToUpdate().size());
            }
            processesInfos.add(infos);
        }
        return new ResponseEntity<List<ProcessInfo>>(processesInfos, HttpStatus.OK);
    }

    @PostMapping(value = "/maintenance/regen-ser-file")
    public ResponseEntity<Void> generateSerFile(@RequestParam MultipartFile restoreFile) {

        try {
            InputStream initialStream = restoreFile.getInputStream();
            byte[] buffer = new byte[initialStream.available()];
            initialStream.read(buffer);

            File origin = File.createTempFile(filesDirectory + File.separator + "restore_extract_RASS", "tmp");
            try (OutputStream out = new FileOutputStream(origin)) {
                out.write(buffer);
            }

            MapsHandler mapsToSerialize = new MapsHandler();
            mapsToSerialize.loadMapsFromFile(origin);
            mapsToSerialize.serializeMaps(filesDirectory + File.separator + "maps.ser");

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error while generating serialized file");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
