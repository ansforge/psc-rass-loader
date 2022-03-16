/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.component;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.esotericsoftware.minlog.Log;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.model.entities.*;
import fr.ans.psc.pscload.model.operations.*;
import fr.ans.psc.pscload.service.EmailService;
import fr.ans.psc.pscload.state.*;
import fr.ans.psc.pscload.utils.FileUtils;
import fr.ans.psc.pscload.visitor.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The Class ProcessRegistryTest.
 */
@Slf4j
@SpringBootTest
class ProcessRegistryTest {
	
	/** The rootpath. */
	static String rootpath = Thread.currentThread().getContextClassLoader().getResource("work").getPath();
	
	/** The registry. */
	private ProcessRegistry registry = new ProcessRegistry(rootpath);

	@Autowired
	CustomMetrics customMetrics;

	@Autowired
	private EmailService emailService;

	@Autowired
	private Kryo kryo;

	@RegisterExtension
	static WireMockExtension httpMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock")).build();

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
		propertiesRegistry.add("extract.download.url",
				() -> httpMockServer.baseUrl() + "/V300/services/extraction/Extraction_ProSanteConnect");
		propertiesRegistry.add("files.directory",
				() -> Thread.currentThread().getContextClassLoader().getResource("work").getPath());
		propertiesRegistry.add("api.base.url", () -> httpMockServer.baseUrl());
		propertiesRegistry.add("use.x509.auth", () -> "false");
		propertiesRegistry.add("enable.scheduler", () -> "true");
		propertiesRegistry.add("scheduler.cron", () -> "0 0 1 15 * ?");
		propertiesRegistry.add("pscextract.base.url", () -> httpMockServer.baseUrl());
	}
	  
	/**
	 * Setup.
	 *
	 * @throws Exception the exception
	 */
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

		httpMockServer.stubFor(any(anyUrl()).willReturn(aResponse().withStatus(200)));
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
		registry.register(id, new LoadProcess(new Submitted()));
		int currentId  = registry.currentId();
		registry.getCurrentProcess().setDownloadedFilename("test");
		Kryo kryo = new Kryo();
		kryo.register(HashMap.class, 9);
		kryo.register(ArrayList.class, 10);
		kryo.register(Professionnel.class, 11);
		kryo.register(ExerciceProfessionnel.class, 12);
		kryo.register(SavoirFaire.class, 13);
		kryo.register(SituationExercice.class, 14);
		kryo.register(RefStructure.class, 15);
		kryo.register(Structure.class, 16);
		kryo.register(ProcessRegistry.class, 17);
		kryo.register(LoadProcess.class, 18);
		kryo.register(ProcessState.class, 19);
		kryo.register(Submitted.class, 20);
		kryo.register(DiffComputed.class, 21);
		kryo.register(ReadyToComputeDiff.class, 22);
		kryo.register(ReadyToExtract.class, 23);
		kryo.register(UploadingChanges.class, 24);
		kryo.register(ChangesApplied.class, 25);
		kryo.register(String[].class, 27);
		kryo.register(ConcurrentHashMap.class, 28);
		kryo.register(UploadInterrupted.class, 29);
		kryo.register(SerializationInterrupted.class, 30);
		kryo.register(OperationMap.class, 31);
		kryo.register(PsCreateMap.class, 32);
		kryo.register(PsUpdateMap.class, 33);
		kryo.register(PsDeleteMap.class, 34);
		kryo.register(StructureCreateMap.class, 35);
		kryo.register(StructureUpdateMap.class, 36);
		kryo.register(StructureDeleteMap.class, 37);
	        
	        
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

	@Test
	public void readRegistrySerFile() throws IOException, DuplicateKeyException {
		File registryFile = new File(rootpath + File.separator + "registry.ser");
		if(registryFile.exists()) {
			registryFile.delete();
		}

		LoadProcess process = generateDiff();
		String[] exclusions = {"90"};
		process.setState(new UploadingChanges(exclusions, httpMockServer.baseUrl()));

		registry.register("1", process);

		// when upload state
//		Kryo kryo = new Kryo();
//		registerWithKryo(kryo);

		FileOutputStream fileOutputStream = new FileOutputStream(registryFile);
		Output output = new Output(fileOutputStream);
		registry.write(kryo, output);
		output.close();
		assert registryFile.exists();

		registry.clear();
		assert registry.isEmpty();

		FileInputStream fileInputStream = new FileInputStream(registryFile);
		Input input = new Input(fileInputStream);
		registry.read(kryo, input);
		input.close();
		registryFile.delete();

		LoadProcess deserializedProcess = registry.getCurrentProcess();

		assertEquals(process.getTmpMapsPath(), deserializedProcess.getTmpMapsPath());
		assertEquals(process.getExtractedFilename(), deserializedProcess.getExtractedFilename());
		assertEquals(process.getState().getClass(), deserializedProcess.getState().getClass());
		assertEquals(process.getTimestamp(), deserializedProcess.getTimestamp());

		PsCreateMap originalPsCreateMap = (PsCreateMap) process.getMaps().stream()
				.filter(map -> OperationType.PS_CREATE.equals(map.getOperation())).findFirst().get();
		PsUpdateMap originalPsUdpdateMap = (PsUpdateMap) process.getMaps().stream()
				.filter(map -> OperationType.PS_UPDATE.equals(map.getOperation())).findFirst().get();
		PsDeleteMap originalPsDeleteMap = (PsDeleteMap) process.getMaps().stream()
				.filter(map -> OperationType.PS_DELETE.equals(map.getOperation())).findFirst().get();

		PsCreateMap deserializedPsCreateMap = (PsCreateMap) deserializedProcess.getMaps().stream()
				.filter(map -> OperationType.PS_CREATE.equals(map.getOperation())).findFirst().get();
		PsUpdateMap deserializedPsUdpdateMap = (PsUpdateMap) deserializedProcess.getMaps().stream()
				.filter(map -> OperationType.PS_UPDATE.equals(map.getOperation())).findFirst().get();
		PsDeleteMap deserializedPsDeleteMap = (PsDeleteMap) deserializedProcess.getMaps().stream()
				.filter(map -> OperationType.PS_DELETE.equals(map.getOperation())).findFirst().get();

		assertEquals(originalPsCreateMap.getNewValues().size(), 1);
		assertEquals(originalPsUdpdateMap.getNewValues().size(), 2);
		assertEquals(originalPsUdpdateMap.getOldValues().size(), 2);
		assertEquals(originalPsDeleteMap.getNewValues().size(), 1);

		log.info(deserializedProcess.getProcessInfos().toString());

		assertEquals(originalPsCreateMap.getNewValues().size(), deserializedPsCreateMap.getNewValues().size());
		assertEquals(originalPsUdpdateMap.getNewValues().size(), deserializedPsUdpdateMap.getNewValues().size());
		assertEquals(originalPsUdpdateMap.getOldValues().size(), deserializedPsUdpdateMap.getOldValues().size());
		assertEquals(originalPsDeleteMap.getNewValues().size(), deserializedPsDeleteMap.getNewValues().size());
	}

	private LoadProcess generateDiff() throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String rootpath = cl.getResource("work").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff(customMetrics));
		File extractFile = FileUtils.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_202112120512.txt");
		p.setExtractedFilename(extractFile.getPath());
		p.nextStep();
		String[] exclusions = {"90"};
		p.setState(new UploadingChanges(exclusions, httpMockServer.baseUrl()));
		p.getState().setProcess(p);
		p.nextStep();
		p.setState(new ChangesApplied(customMetrics, httpMockServer.baseUrl(), emailService));
		p.getState().setProcess(p);
		p.nextStep();

		LoadProcess p2 = new LoadProcess(new ReadyToComputeDiff(customMetrics));
		File extractFile2 = FileUtils.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_202112120515.txt");
		p2.setExtractedFilename(extractFile2.getPath());
		p2.getState().setProcess(p2);
		p2.nextStep();

		return p2;
	}

	private void registerWithKryo(Kryo kryo) {
		kryo.register(HashMap.class, 9);
		kryo.register(ArrayList.class, 10);
		kryo.register(Professionnel.class, 11);
		kryo.register(ExerciceProfessionnel.class, 12);
		kryo.register(SavoirFaire.class, 13);
		kryo.register(SituationExercice.class, 14);
		kryo.register(RefStructure.class, 15);
		kryo.register(Structure.class, 16);
		kryo.register(ProcessRegistry.class, 17);
		kryo.register(LoadProcess.class, 18);
		kryo.register(ProcessState.class, 19);
		kryo.register(Submitted.class, 20);
		kryo.register(DiffComputed.class, 21);
		kryo.register(ReadyToComputeDiff.class, 22);
		kryo.register(ReadyToExtract.class, 23);
		kryo.register(UploadingChanges.class, 24);
		kryo.register(ChangesApplied.class, 25);
		kryo.register(String[].class, 27);
		kryo.register(ConcurrentHashMap.class, new MapSerializer<ConcurrentHashMap<String, RassEntity>>(), 28);
		kryo.register(UploadInterrupted.class, 29);
		kryo.register(SerializationInterrupted.class, 30);
		kryo.register(PsCreateMap.class, 32);
		kryo.register(PsUpdateMap.class, 33);
		kryo.register(PsDeleteMap.class, 34);
		kryo.register(StructureCreateMap.class, 35);
		kryo.register(StructureUpdateMap.class, 36);
		kryo.register(StructureDeleteMap.class, 37);
	}

	@Test
	public void readRegistryFileTest() throws IOException {
//		Log.TRACE();
		File registryFile = FileUtils.copyFileToWorkspace("pscload-registry.ser");
		FileInputStream fileInputStream = new FileInputStream(registryFile);
		Input input = new Input(fileInputStream);
		registry.read(kryo, input);
		input.close();
	}

}
