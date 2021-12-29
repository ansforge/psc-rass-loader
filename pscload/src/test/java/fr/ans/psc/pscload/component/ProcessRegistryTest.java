/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.model.StructureRef;
import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.metrics.UploadMetrics;
import fr.ans.psc.pscload.model.ExerciceProfessionnel;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.SavoirFaire;
import fr.ans.psc.pscload.model.SituationExercice;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.ChangesApplied;
import fr.ans.psc.pscload.state.DiffComputed;
import fr.ans.psc.pscload.state.Idle;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.ReadyToComputeDiff;
import fr.ans.psc.pscload.state.ReadyToExtract;
import fr.ans.psc.pscload.state.UploadingChanges;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ProcessRegistryTest.
 */
@Slf4j
class ProcessRegistryTest {
	
	/** The rootpath. */
	static String rootpath = Thread.currentThread().getContextClassLoader().getResource("work").getPath();
	
	/** The registry. */
	private ProcessRegistry registry = new ProcessRegistry(rootpath);
	  
	@BeforeEach
	void setup() throws Exception {
		registry.clear();
		// clear work directory
		File outputfolder = new File(Thread.currentThread().getContextClassLoader().getResource("work").getPath());
		File[] files = outputfolder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				f.delete();
			}
		}
	}
	
	/**
	 * Serialization test.
	 *
	 * @throws Exception the exception
	 */
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
		Kryo kryo = new Kryo();
	        kryo.register(HashMap.class, 9);
	        kryo.register(ArrayList.class, 10);
	        kryo.register(Professionnel.class, 11);
	        kryo.register(ExerciceProfessionnel.class, 12);
	        kryo.register(SavoirFaire.class, 13);
	        kryo.register(SituationExercice.class, 14);
	        kryo.register(StructureRef.class, 15);
	        kryo.register(Structure.class, 16);
	        kryo.register(ProcessRegistry.class, 17);
	        kryo.register(LoadProcess.class, 18);
	        kryo.register(ProcessState.class, 19);
	        kryo.register(Idle.class, 20);
	        kryo.register(DiffComputed.class, 21);
	        kryo.register(ReadyToComputeDiff.class, 22);
	        kryo.register(ReadyToExtract.class, 23);
	        kryo.register(UploadingChanges.class, 24);
	        kryo.register(ChangesApplied.class, 25);
	        kryo.register(UploadMetrics.class, 26);
	        
	        
		FileOutputStream fileOutputStream = new FileOutputStream(registryFile);
		Output output = new Output(fileOutputStream);
		registry.write(kryo, output);
		output.close();

	    FileInputStream fileInputStream = new FileInputStream(registryFile);
		Input input = new Input(fileInputStream);
		registry.read(kryo, input);

	    registry.getCurrentProcess().setDownloadedFilename("test2");
	    FileInputStream fileInputStream2 = new FileInputStream(registryFile);
		Input input2 = new Input(fileInputStream2);
		registry.read(kryo, input2);
		input2.close();
		
		assertEquals(currentId, registry.currentId());
		assertEquals("test", registry.getCurrentProcess().getDownloadedFilename());
	}

}
