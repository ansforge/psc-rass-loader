/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.EmailTemplate;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.model.Stage;
import fr.ans.psc.pscload.service.EmailService;
import fr.ans.psc.pscload.state.ChangesApplied;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.UploadInterrupted;
import fr.ans.psc.pscload.state.UploadingChanges;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * The Class PscloadApplication.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class PscloadApplication {

    /**
     * The registry.
     */
    @Autowired
    private ProcessRegistry registry;

    /**
     * The custom metrics.
     */
    @Autowired
    private CustomMetrics customMetrics;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Kryo kryo;

    @Value("${files.directory:.}")
    private String filesDirectory;

    @Value("${pscextract.base.url}")
    private String pscextractBaseUrl;

    @Value("${snitch:false}")
    private boolean debug;

    @Value("${deactivation.excluded.profession.codes}")
    private String[] excludedProfessions;

    @Value("${api.base.url}")
    private String apiBaseUrl;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(PscloadApplication.class, args);
        applicationContext.start();
    }

    /**
     * The listener interface for receiving contextStarted events.
     * The class that is interested in processing a contextStarted
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addContextStartedListener<code> method. When
     * the contextStarted event occurs, that object's appropriate
     * method is invoked.
     *
     * @see ContextStartedEvent
     */
    @Component
    class ContextStartedListener implements ApplicationListener<ContextRefreshedEvent> {


        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            // Load registry if exists
            log.info("Search registry to restore");
            File registryFile = new File(filesDirectory + File.separator + "registry.ser");
            if (registryFile.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(registryFile);
                    Input input = new Input(fileInputStream);
                    registry.read(kryo, input);
                    input.close();
                    if (debug) {
                        registryFile.delete();
                    }
                    log.info("Registry restored");
                } catch (IOException | KryoException e) {
                    log.warn("Unable to restore registry, start with an empty registry", e);
                    if (debug) {
                        registryFile.delete();
                    }
                    registry.clear();
                }

                // RESUME PROCESS
                LoadProcess process = registry.getCurrentProcess();
                if (process != null) {
                    Class<? extends ProcessState> stateClass = process.getState().getClass();

                    // checking expirability of the state of a process is equivalent to check its resumability :
                    // a non-expirable state is resumable at application restart, an expirable one must be unregistered
                    if (!process.getState().isExpirable()) {

                        // UploadInterrupted state has its own behavior :
                        // as we don't know why UploadingChanges.nextStep() has thrown an UploadException,
                        // we want to let admins decide whether or not to resume process
                        // so we send an alert email and admins can resume or abort process
                        if (stateClass.equals(UploadInterrupted.class)) {
                            log.info("Stage is UploadInterrupted, will not be automatically resumed.");
                            customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE)
                                    .set(Stage.UPLOAD_CHANGES_STARTED.value);
                            emailService.sendMail(EmailTemplate.UPLOAD_REST_INTERRUPTION);
                            return;
                        }

                        log.info(String.format("Stage is %s, will be automatically resumed.", stateClass.getSimpleName()));
                        ForkJoinPool.commonPool().submit(() -> {
                            try {
                                if (stateClass.equals(UploadingChanges.class)) {
                                    // upload changes
                                    customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE)
                                            .set(Stage.UPLOAD_CHANGES_STARTED.value);

                                    process.setState(new UploadingChanges(excludedProfessions, apiBaseUrl));
                                    process.nextStep();

                                }
                                customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE).set(70);
                                // Step 5 : call pscload
                                process.setState(new ChangesApplied(customMetrics, pscextractBaseUrl, emailService));
                                process.nextStep();
                                registry.unregister(process.getId());
                                customMetrics.getAppMiscGauges().get(CustomMetrics.MiscCustomMetric.STAGE).set(0);
                            } catch (LoadProcessException e) {
                                log.error("error when uploading changes", e);
                            }
                        });
                    } else {
                        log.info("Stage is not resumable, process is aborted");
                        registry.clear();
                    }
                }
            } else {
                log.info("no registry file has been found.");
            }
        }
    }

    /**
     * The listener interface for receiving contextClosed events.
     * The class that is interested in processing a contextClosed
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addContextClosedListener<code> method. When
     * the contextClosed event occurs, that object's appropriate
     * method is invoked.
     *
     * @see ContextClosedEvent
     */
    @Component
    class ContextClosedListener implements ApplicationListener<ContextClosedEvent> {

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            // Interrupt the worker thread.
            ForkJoinPool.commonPool().shutdownNow();
            // Save the registry if not empty
            if (!registry.isEmpty()) {
                log.info("Try to save registry");
                try {
                    File registryFile = new File(filesDirectory + File.separator + "registry.ser");
                    FileOutputStream fileOutputStream = new FileOutputStream(registryFile);
                    Output output = new Output(fileOutputStream);
                    registry.write(kryo, output);
                    output.close();

                    registry.clear();
                    FileInputStream fileInputStream = new FileInputStream(registryFile);
                    Input input = new Input(fileInputStream);
                    registry.read(kryo, input);
                    input.close();

                    log.info("Registry saved successfully !");
                } catch (IOException e) {
                    log.error("Unable to save registry", e);
                }
            }
        }
    }
}
