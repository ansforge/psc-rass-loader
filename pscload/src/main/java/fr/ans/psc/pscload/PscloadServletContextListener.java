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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PscloadServletContextListener implements ServletContextListener {

	@Autowired
	ProcessRegistry registry;

	@Value("${files.directory}")
	private String filesDirectory;

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// Wait for upload finished
		//TODO configure timeout
		ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.MINUTES);
		// Save the registry
		try {
			File registryFile = new File(filesDirectory + File.separator + "registry.ser");
			FileOutputStream fileOutputStream
		     = new FileOutputStream(registryFile);
		    ObjectOutputStream oos
		     = new ObjectOutputStream(fileOutputStream);
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
				FileInputStream fileInputStream
			     = new FileInputStream(registryFile);
			    ObjectInputStream ois
			     = new ObjectInputStream(fileInputStream);
				registry.readExternal(ois);
				registryFile.delete();	
			} catch (IOException e) {
				log.error("Unable to restore registry I/O error", e);
			} catch (ClassNotFoundException e) {
				log.error("Unable to restore registry : file not compatible", e);
			}
		}
		//TODO republish stage metrics
		// AND RESUME PROCESS
	}
}