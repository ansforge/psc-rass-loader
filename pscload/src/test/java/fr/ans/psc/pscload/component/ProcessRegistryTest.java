/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscload.component;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.model.entities.ExerciceProfessionnel;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.entities.SavoirFaire;
import fr.ans.psc.pscload.model.entities.SituationExercice;
import fr.ans.psc.pscload.model.entities.Structure;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.model.operations.OperationMapSerializer;
import fr.ans.psc.pscload.model.operations.PsCreateMap;
import fr.ans.psc.pscload.model.operations.PsDeleteMap;
import fr.ans.psc.pscload.model.operations.PsUpdateMap;
import fr.ans.psc.pscload.service.EmailService;
import fr.ans.psc.pscload.state.ChangesApplied;
import fr.ans.psc.pscload.state.DiffComputed;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.ReadyToComputeDiff;
import fr.ans.psc.pscload.state.ReadyToExtract;
import fr.ans.psc.pscload.state.SerializationInterrupted;
import fr.ans.psc.pscload.state.Submitted;
import fr.ans.psc.pscload.state.UploadInterrupted;
import fr.ans.psc.pscload.state.UploadingChanges;
import fr.ans.psc.pscload.utils.FileUtils;
import fr.ans.psc.pscload.model.operations.OperationType;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ProcessRegistryTest.
 */
@Slf4j
@SpringBootTest
class ProcessRegistryTest {

	/** The rootpath. */
	static String rootpath = Thread.currentThread().getContextClassLoader().getResource("work").getPath();

	/** The registry. */
//	private ProcessRegistry registry = new ProcessRegistry(rootpath);

	@Autowired
	ProcessRegistry registry;

	/** The custom metrics. */
	@Autowired
	CustomMetrics customMetrics;

	@Autowired
	private EmailService emailService;

	@Autowired
	private Kryo kryo;

	/** The http mock server. */
	@RegisterExtension
	static WireMockExtension httpMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock")).build();

	/**
	 * Register pg properties.
	 *
	 * @param propertiesRegistry the properties registry
	 */
	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
		propertiesRegistry.add("extract.download.url",
				() -> httpMockServer.baseUrl() + "/V300/services/extraction/Extraction_ProSanteConnect");
		propertiesRegistry.add("files.directory",
				() -> Thread.currentThread().getContextClassLoader().getResource("work").getPath());
		propertiesRegistry.add("api.base.url", () -> httpMockServer.baseUrl() + "/psc-api-maj/api");
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
	void setup() {
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
//		httpMockServer.stubFor(put("v2/ps/1111").willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
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
		OperationMapSerializer operationMapSerializer = new OperationMapSerializer();
		kryo.register(HashMap.class, 9);
		kryo.register(ArrayList.class, 10);
		kryo.register(Professionnel.class, 11);
		kryo.register(ExerciceProfessionnel.class, 12);
		kryo.register(SavoirFaire.class, 13);
		kryo.register(SituationExercice.class, 14);
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
		kryo.register(OperationMap.class, operationMapSerializer, 31);
		kryo.register(PsCreateMap.class, operationMapSerializer, 32);
		kryo.register(PsUpdateMap.class, operationMapSerializer,33);
		kryo.register(PsDeleteMap.class, operationMapSerializer,34);


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

	/**
	 * Read registry ser file.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DuplicateKeyException the duplicate key exception
	 */
	@Test
	public void readRegistrySerFile() throws IOException, DuplicateKeyException {
		File registryFile = new File(rootpath + File.separator + "registry.ser");
		if(registryFile.exists()) {
			registryFile.delete();
		}

		LoadProcess process = generateDiff("Extraction_ProSanteConnect_Personne_activite_202112120512.txt", "Extraction_ProSanteConnect_Personne_activite_202112120515.txt");
		String[] exclusions = {"90"};
		process.setState(new UploadingChanges(exclusions, httpMockServer.baseUrl()));

		registry.register("1", process);

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
				.filter(map -> OperationType.CREATE.equals(map.getOperation())).findFirst().get();
		PsUpdateMap originalPsUdpdateMap = (PsUpdateMap) process.getMaps().stream()
				.filter(map -> OperationType.UPDATE.equals(map.getOperation())).findFirst().get();
		PsDeleteMap originalPsDeleteMap = (PsDeleteMap) process.getMaps().stream()
				.filter(map -> OperationType.DELETE.equals(map.getOperation())).findFirst().get();

		PsCreateMap deserializedPsCreateMap = (PsCreateMap) deserializedProcess.getMaps().stream()
				.filter(map -> OperationType.CREATE.equals(map.getOperation())).findFirst().get();
		PsUpdateMap deserializedPsUdpdateMap = (PsUpdateMap) deserializedProcess.getMaps().stream()
				.filter(map -> OperationType.UPDATE.equals(map.getOperation())).findFirst().get();
		PsDeleteMap deserializedPsDeleteMap = (PsDeleteMap) deserializedProcess.getMaps().stream()
				.filter(map -> OperationType.DELETE.equals(map.getOperation())).findFirst().get();

		assertEquals(originalPsCreateMap.size(), 1);
		assertEquals(originalPsUdpdateMap.size(), 2);
		assertEquals(originalPsUdpdateMap.size(), 2);
		assertEquals(originalPsUdpdateMap.getOldValues().size(), 2);
		assertEquals(originalPsDeleteMap.size(), 1);

		assertEquals(originalPsCreateMap.size(), deserializedPsCreateMap.size());
		assertEquals(originalPsUdpdateMap.size(), deserializedPsUdpdateMap.size());
		assertEquals(originalPsUdpdateMap.getOldValues().size(), deserializedPsUdpdateMap.getOldValues().size());
		assertEquals(originalPsDeleteMap.size(), deserializedPsDeleteMap.size());

		if(registryFile.exists()) {
			registryFile.delete();
		}
	}

	private LoadProcess generateDiff(String fileName1, String fileName2) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String rootpath = cl.getResource("work").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff(customMetrics, httpMockServer.baseUrl()));
		File extractFile = FileUtils.copyFileToWorkspace(fileName1);
		p.setExtractedFilename(extractFile.getPath());
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(410)));
		p.nextStep();
		String[] exclusions = {"90"};
		p.setState(new UploadingChanges(exclusions, httpMockServer.baseUrl()));
		p.getState().setProcess(p);
		p.nextStep();
		p.setState(new ChangesApplied(customMetrics, httpMockServer.baseUrl(), emailService));
		p.getState().setProcess(p);
		p.nextStep();

		LoadProcess p2 = new LoadProcess(new ReadyToComputeDiff(customMetrics, httpMockServer.baseUrl()));
		File extractFile2 = FileUtils.copyFileToWorkspace(fileName2);
		p2.setExtractedFilename(extractFile2.getPath());
		p2.getState().setProcess(p2);
		File dayOneFile = new File(Thread.currentThread().getContextClassLoader().getResource("day-one.json").getPath());
		String dayOneJSON = Files.readString(dayOneFile.toPath());
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(200).withBody(dayOneJSON)));
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("1"))
				.willReturn(aResponse().withStatus(410)));
		p2.nextStep();

		return p2;
	}
}
