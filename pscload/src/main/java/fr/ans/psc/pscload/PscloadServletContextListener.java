/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.ChangesApplied;
import fr.ans.psc.pscload.state.DiffComputed;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The listener interface for receiving pscloadServletContext events.
 * The class that is interested in processing a pscloadServletContext
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addPscloadServletContextListener<code> method. When
 * the pscloadServletContext event occurs, that object's appropriate
 * method is invoked.
 *
 * @see PscloadServletContextEvent
 */
@Slf4j
public class PscloadServletContextListener implements ServletContextListener {

	/** The registry. */
	@Autowired
	ProcessRegistry registry;

	/** The custom metrics. */
	@Autowired
	CustomMetrics customMetrics;

	@Value("${files.directory}")
	private String filesDirectory;

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// Wait for upload finished
		// TODO configure timeout
		ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS);
		// Save the registry
		try {
			File registryFile = new File(filesDirectory + File.separator + "registry.ser");
			FileOutputStream fileOutputStream = new FileOutputStream(registryFile);
			ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
			registry.writeExternal(oos);
		} catch (IOException e) {
			log.error("Unable to save registry", e);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// Load registry if exists
		File registryFile = new File(filesDirectory + File.separator + "registry.ser");
		if (registryFile.exists()) {
			try {
				FileInputStream fileInputStream = new FileInputStream(registryFile);
				ObjectInputStream ois = new ObjectInputStream(fileInputStream);
				registry.readExternal(ois);
				registryFile.delete();
			} catch (IOException e) {
				log.error("Unable to restore registry I/O error", e);
			} catch (ClassNotFoundException e) {
				log.error("Unable to restore registry : file not compatible", e);
			}
		}

		// RESUME PROCESS
		LoadProcess process = registry.getCurrentProcess();
		Class<? extends ProcessState> stateClass = process.getState().getClass();
		if (stateClass.equals(DiffComputed.class)) {
			DiffComputed state = (DiffComputed) registry.getCurrentProcess().getState();
			if (state.isRunning()) {
				ForkJoinPool.commonPool().submit(() -> {
					try {
						// upload changes
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
				});
			} else {
				log.info("Upload was not running when shutdown, process is aborted");
				registry.clear();
			}

		} else {
			log.info("Stage is not resumable, process is aborted");
			registry.clear();
		}
	}
}