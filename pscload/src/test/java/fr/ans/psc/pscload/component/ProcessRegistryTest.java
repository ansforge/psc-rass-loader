package fr.ans.psc.pscload.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.Idle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
class ProcessRegistryTest {
	String rootpath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
	ProcessRegistry registry = new ProcessRegistry(rootpath);
	
	@Test
	void serializationTest() throws Exception {
		File registryFile = new File(rootpath + File.separator + "registry.ser");
		if(registryFile.exists()) {
			registryFile.delete();
		}
		String id = Integer.toString(registry.nextId());
		registry.register(id, new LoadProcess(new Idle()));
		int currentId  = registry.currentId();
		registry.getCurrentProcess().setDownloadedFilename("test");
		FileOutputStream fileOutputStream
	     = new FileOutputStream(registryFile);
	    ObjectOutputStream oos
	     = new ObjectOutputStream(fileOutputStream);
	    registry.writeExternal(oos);
	    FileInputStream fileInputStream
	     = new FileInputStream(registryFile);
	    ObjectInputStream ois
	     = new ObjectInputStream(fileInputStream);
	    registry.getCurrentProcess().setDownloadedFilename("test2");
		registry.readExternal(ois);
		assertEquals(currentId, registry.currentId());
		assertEquals("test", registry.getCurrentProcess().getDownloadedFilename());
	}

}
