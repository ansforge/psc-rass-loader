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
package fr.ans.psc.pscload.state;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import fr.ans.psc.model.FirstName;
import fr.ans.psc.pscload.model.entities.Professionnel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.service.EmailService;
import fr.ans.psc.pscload.utils.FileUtils;
import fr.ans.psc.pscload.model.operations.OperationType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class FileExtractedTest.
 */
@Slf4j
@SpringBootTest
class ReadyToComputeDiffTest {

	/** The custom metrics. */
	@Autowired
	CustomMetrics customMetrics;

	@Autowired
	private EmailService emailService;

	/** The http mock server. */
	@RegisterExtension
	static WireMockExtension httpMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock").extensions(new PostServeAction() {
				@Override
				public String getName() {
					return "something";
				}
				@Override
				public void doGlobalAction(ServeEvent serveEvent, Admin admin) {
//					log.error(serveEvent.getRequest().toString());
//					log.error(serveEvent.getResponse().toString());
				}
			})).build();

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
		propertiesRegistry.add("api.base.url", () -> httpMockServer.baseUrl());
		propertiesRegistry.add("use.x509.auth", () -> "false");
		propertiesRegistry.add("enable.scheduler", () -> "true");
		propertiesRegistry.add("scheduler.cron", () -> "0 0 1 15 * ?");
		propertiesRegistry.add("pscextract.base.url", () -> httpMockServer.baseUrl());
	}

	/**
	 * Sets the up.
	 */
	@BeforeEach
	void setUp() {
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
	 * Initial diff task test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName("Initial diff with no old ser file and 5 ps")
	void initialDiffTaskTest() throws Exception {
		String rootpath = Thread.currentThread().getContextClassLoader().getResource("work").getPath();
//		File mapser = new File(rootpath + File.separator + "maps.ser");
//		if (mapser.exists()) {
//			mapser.delete();
//		}
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff(List.of("60"),customMetrics, httpMockServer.baseUrl()));
		File extractFile = FileUtils.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_202112120512.txt");
		p.setExtractedFilename(extractFile.getPath());

		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(410)));

		p.nextStep();
		OperationMap<String, RassEntity> psToCreate = p.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.CREATE))
				.findFirst().get();
        OperationMap<String, RassEntity> psToDelete = p.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.DELETE))
				.findFirst().get();
		assertEquals(5, psToCreate.size());
		assertEquals(0, psToDelete.size());
	}

	/**
	 * Diff task test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName(" diff with 1 supp, 2 modifs and 1 add")
	void diffTaskTest() throws Exception {
//		ClassLoader cl = Thread.currentThread().getContextClassLoader();
//		String rootpath = cl.getResource("work").getPath();
//		File mapser = new File(rootpath + File.separator + "maps.ser");
//		if (mapser.exists()) {
//			mapser.delete();
//		}
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff(List.of("60"),customMetrics, httpMockServer.baseUrl()));
		File extractFile = FileUtils.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_202112120512.txt");
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

		LoadProcess p2 = new LoadProcess(new ReadyToComputeDiff(List.of("60"),customMetrics, httpMockServer.baseUrl()));
		File extractFile2 = FileUtils.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_202112120515.txt");
		p2.setExtractedFilename(extractFile2.getPath());
		p2.getState().setProcess(p2);
		File dayOneFile = new File(Thread.currentThread().getContextClassLoader().getResource("day-one.json").getPath());
		String dayOneJSON = Files.readString(dayOneFile.toPath());
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(200).withBody(dayOneJSON)));
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("1"))
				.willReturn(aResponse().withStatus(410)));
		p2.nextStep();
		OperationMap<String, RassEntity> psToCreate2 = p2.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.CREATE))
				.findFirst().get();
        OperationMap<String, RassEntity> psToDelete2 = p2.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.DELETE))
				.findFirst().get();
        OperationMap<String, RassEntity> psToUpdate2 = p2.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.UPDATE))
				.findFirst().get();
		assertEquals(1,psToDelete2.size());
		assertEquals(1,psToCreate2.size());
		assertEquals(2, psToUpdate2.size());
		
		// Test that a PS in database with a NationalId which is an UUID (PSI), not present in RASS file, is not deleted
		LoadProcess p3 = new LoadProcess(new ReadyToComputeDiff(List.of("60"),customMetrics, httpMockServer.baseUrl()));
		File extractFile3 = FileUtils.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_202112120515.txt");
		p3.setExtractedFilename(extractFile3.getPath());
		p3.getState().setProcess(p3);
		File uuidFile = new File(Thread.currentThread().getContextClassLoader().getResource("test-uuid.json").getPath());
		String uuidJSON = Files.readString(uuidFile.toPath());
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(200).withBody(uuidJSON)));
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("1"))
				.willReturn(aResponse().withStatus(410)));
		p3.nextStep();

        OperationMap<String, RassEntity> psToDelete3 = p3.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.DELETE))
				.findFirst().get();

        assertFalse(psToDelete3.values().stream().anyMatch(re -> ( ReadyToComputeDiff.isValidUUID(((Professionnel)re).getNationalId()))));
        assertFalse(psToDelete3.values().stream().anyMatch(re -> "TESTUUID".equals(((Professionnel)re).getId())));
    
		// Test that a PS in database with a NationalId which is an UUID (PSI), also in
		// present in RASS file, has only its professions updated
		LoadProcess p4 = new LoadProcess(
				new ReadyToComputeDiff(List.of("60"), customMetrics, httpMockServer.baseUrl()));
		File extractFile4 = FileUtils
				.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_fakeUUID_profession_202112140854.txt");
		p4.setExtractedFilename(extractFile4.getPath());
		p4.getState().setProcess(p4);
		File uuidProfFile = new File(Thread.currentThread().getContextClassLoader().getResource("test-uuid-profession.json").getPath());
		String uuidProfFileJSON = Files.readString(uuidProfFile.toPath());
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0")).willReturn(
				aResponse().withHeader("Content-Type", "application/json").withStatus(200).withBody(uuidProfFileJSON)));
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("1"))
				.willReturn(aResponse().withStatus(410)));
		p4.nextStep();
		
		OperationMap<String, RassEntity> psToUpdate4 = p4.getMaps().stream()
				.filter(map -> map.getOperation().equals(OperationType.UPDATE)).findFirst().get();
		Professionnel ps = (Professionnel) psToUpdate4.get("550e8400-e29b-41d4-a716-446655440000");
		assertEquals("tonkine@gmail.com", ps.getEmail()); // Database value unchanged
		assertEquals("31/10/1990", ps.getDateOfBirth()); // Database value unchanged
		assertEquals("NEW_STREET",
				ps.getProfessions().get(0).getWorkSituations().get(0).getStructure().getStreetLabel()); // RASS update
		
		
		
		// Test that a PS from RASS is not created if its Id is in a ps's Ids in db
		LoadProcess p5 = new LoadProcess(
				new ReadyToComputeDiff(List.of("60"), customMetrics, httpMockServer.baseUrl()));
		File extractFile5 = FileUtils
				.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_ids_202112140856.txt");
		p5.setExtractedFilename(extractFile5.getPath());
		p5.getState().setProcess(p5);
		File testIdsFile = new File(Thread.currentThread().getContextClassLoader().getResource("test-ids.json").getPath());
		String testIdsJSON = Files.readString(testIdsFile.toPath());
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0")).willReturn(
				aResponse().withHeader("Content-Type", "application/json").withStatus(200).withBody(testIdsJSON)));
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("1"))
				.willReturn(aResponse().withStatus(410)));
		p5.nextStep();
		
		OperationMap<String, RassEntity> psToCreate5 = p5.getMaps().stream()
				.filter(map -> map.getOperation().equals(OperationType.CREATE)).findFirst().get();
		assertThat(psToCreate5).isEmpty(); // No user to create even if their Id is not in db because it's in Ids
		OperationMap<String, RassEntity> psToUpdate5 = p5.getMaps().stream()
				.filter(map -> map.getOperation().equals(OperationType.UPDATE)).findFirst().get();

		Professionnel ps2 = (Professionnel) psToUpdate5.get("810107592544");
		assertEquals("tonkine@gmail.com", ps2.getEmail()); // Database value unchanged
		assertEquals("31/10/1990", ps2.getDateOfBirth()); // Database value unchanged
		assertEquals("NEW_STREET",
				ps2.getProfessions().get(0).getWorkSituations().get(0).getStructure().getStreetLabel()); // RASS update
		
		assertEquals(1, psToUpdate5.size()); // Second user PINGOT needs no update as profession is unchanged
		
		
		// Ids cleaning if not anymore in RASS file
		LoadProcess p6 = new LoadProcess(
				new ReadyToComputeDiff(List.of("60"), customMetrics, httpMockServer.baseUrl()));
		File extractFile6 = FileUtils
				.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_ids_removal_202112140858.txt");
		p6.setExtractedFilename(extractFile6.getPath());
		p6.getState().setProcess(p6);
		File testIdsDeletionFile = new File(Thread.currentThread().getContextClassLoader().getResource("test-ids-removal.json").getPath());
		String testIdsDeletionFileJSON = Files.readString(testIdsDeletionFile.toPath());
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0")).willReturn(
				aResponse().withHeader("Content-Type", "application/json").withStatus(200).withBody(testIdsDeletionFileJSON)));
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("1"))
				.willReturn(aResponse().withStatus(410)));
		p6.nextStep();
		

		OperationMap<String, RassEntity> psToUpdate6 = p6.getMaps().stream()
				.filter(map -> map.getOperation().equals(OperationType.UPDATE)).findFirst().get();
		OperationMap<String, RassEntity> psToDelete6 = p6.getMaps().stream()
				.filter(map -> map.getOperation().equals(OperationType.DELETE)).findFirst().get();
		 // HIRE is not in RASS file but in Db with uuid, RASS ids will be removed, as one of its ids is RPPS, professions will also be removed
		assertTrue(psToUpdate6.containsKey("550e8400-e29b-41d4-a716-446655440000")); 
		Professionnel ps01 = (Professionnel) psToUpdate6.get("550e8400-e29b-41d4-a716-446655440000");
		assertTrue(ps01.getIds().contains("550e8400-e29b-41d4-a716-446655440000") && ps01.getIds().size() ==1);
		assertTrue(ps01.getProfessions().isEmpty());
		// PINGOT is in RASS file with an RPPS id, ADELI won't be removed
		assertFalse(psToUpdate6.containsKey("550e8400-e29b-41d4-a716-446655440001"));
		
		// MUNOZ is not anymore in RASS file and in Db with RASS National ID, he must be deleted, not updated
		assertTrue(!psToUpdate6.containsKey("8010000207") && psToDelete6.containsKey("8010000207"));
	}
	
	
	@Test
	@DisplayName(" Test origin")
	void originTest() throws Exception {
		LoadProcess process = new LoadProcess(
				new ReadyToComputeDiff(List.of("60"), customMetrics, httpMockServer.baseUrl()));
		File extractFile = FileUtils
				.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_202112120516.txt");
		process.setExtractedFilename(extractFile.getPath());
		process.getState().setProcess(process);
		File dayTwoFile = new File(
				Thread.currentThread().getContextClassLoader().getResource("empty.json").getPath());
		String dayTwoJSON = Files.readString(dayTwoFile.toPath());
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0")).willReturn(
				aResponse().withHeader("Content-Type", "application/json").withStatus(200).withBody(dayTwoJSON)));
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("1"))
				.willReturn(aResponse().withStatus(410)));
		process.nextStep();

		OperationMap<String, RassEntity> psToCreate = process.getMaps().stream()
				.filter(map -> map.getOperation().equals(OperationType.CREATE)).findFirst().get();

		Map<Character, String> origin = Map.of('0', "ADELI", '1', "CAB_ADELI", '3', "FINESS", '4', "SIREN", '5',
				"SIRET", '6', "CAB_RPPS", '8', "RPPS");

		for (RassEntity entity : psToCreate.values()) {
			assertEquals(((Professionnel) entity).getOrigin(),
					origin.get(((Professionnel) entity).getNationalId().charAt(0)));
			assertEquals(1, ((Professionnel) entity).getQuality());
		}

	}
	

	/**
	 * Diff from large file.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName("initial diff from large file (100000 lines)")
	public void diffFromLargeFile() throws Exception {
//		ClassLoader cl = Thread.currentThread().getContextClassLoader();
//		String rootpath = cl.getResource("work").getPath();
//		File mapser = new File(rootpath + File.separator + "maps.ser");
//		if (mapser.exists()) {
//			mapser.delete();
//		}
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff(List.of("60"),customMetrics, httpMockServer.baseUrl()));
		File extractFile = FileUtils.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_202112140852.txt");
		p.setExtractedFilename(extractFile.getPath());
		httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(410)));
		p.nextStep();
		OperationMap<String, RassEntity> psToCreate = p.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.CREATE))
				.findFirst().get();
		assertEquals(psToCreate.size(), 99171);
	}

	@Test
	@Disabled
	@DisplayName("check order impact on hashCode and equals methods")
	public void checkDifferentOrderForPs() throws IOException {
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff(List.of("60"),customMetrics, httpMockServer.baseUrl()));
		File file1 = FileUtils.copyFileToWorkspace("2WorkSituationsOrder1");
		ReadyToComputeDiff state = (ReadyToComputeDiff) p.getState();
		Map<String, Professionnel> order1Map = state.loadMapsFromFile(file1);

		File file2 = FileUtils.copyFileToWorkspace("2WorkSituationsOrder2");
		Map<String, Professionnel> order2Map = state.loadMapsFromFile(file2);

		assertEquals(1,order1Map.size());
		assertEquals(1, order2Map.size());

		MapDifference<String, Professionnel> diffPs = Maps.difference(order1Map, order2Map);
		assertEquals(0, diffPs.entriesDiffering().size());
		assertEquals(order1Map.get("810107592544").hashCode(), order2Map.get("810107592544").hashCode());
		assertEquals(order1Map.get("810107592544"), order2Map.get("810107592544"));
	}

	@Test
	@Disabled
	@DisplayName("check that the order of first names is handled correctly")
	public void checkCorrectFirstNameOrder() throws IOException {
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff(List.of("60"),customMetrics, httpMockServer.baseUrl()));
		File file1 = FileUtils.copyFileToWorkspace("FirstNameOrder");
		ReadyToComputeDiff state = (ReadyToComputeDiff) p.getState();
		Map<String, Professionnel> nameOrderMap = state.loadMapsFromFile(file1);

		assertEquals(3, nameOrderMap.size());

		Professionnel professionnel123 = nameOrderMap.get("1");
		Professionnel professionnel231 = nameOrderMap.get("2");
		Professionnel professionnel31 = nameOrderMap.get("3");

		professionnel123.getFirstNames().sort((Comparator.comparing(FirstName::getOrder)));
		professionnel231.getFirstNames().sort((Comparator.comparing(FirstName::getOrder)));
		professionnel31.getFirstNames().sort((Comparator.comparing(FirstName::getOrder)));

		for (Professionnel professionnel : Arrays.asList(professionnel123, professionnel231, professionnel31)) {
			System.out.println("\nChecking the order of first names in "+professionnel.getFirstNames());
			for (int i = 0; i < professionnel.getFirstNames().size(); i++) {
				assertEquals(i, professionnel.getFirstNames().get(i).getOrder());
				System.out.println("Order of "+professionnel.getFirstNames().get(i)+" is "+professionnel.getFirstNames().get(i).getOrder());
			}
		}
	}
}
