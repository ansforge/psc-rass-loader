/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.StructureApi;
import fr.ans.psc.model.Structure;
import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.service.LoadProcess;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class DiffComputedStateTest.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
@AutoConfigureMockMvc
public class UploadingStateTest {

	
	/** The mock mvc. */
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private CustomMetrics customMetrics;
	
	@Autowired
	private ProcessRegistry registry;
	
	/** The http api mock server. */
	@RegisterExtension
	static WireMockExtension httpApiMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig()
					.dynamicPort()
					.usingFilesUnderClasspath("wiremock/api"))
			.configureStaticDsl(true).build();

	// For use with mockMvc
	@DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
		propertiesRegistry.add("api.base.url", 
          () -> httpApiMockServer.baseUrl());
		propertiesRegistry.add("deactivation.excluded.profession.codes", () -> "0");
    }
	
	/**
	 * Api call test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName("Structure Api Call")
	void apiCallTest() throws Exception {
		httpApiMockServer.stubFor(get("/structure/1")
			    .willReturn(aResponse()
			    		.withBodyFile("structure1.json")
			    		.withHeader("Content-Type", "application/json")
			    		.withStatus(200)));
		ApiClient client = new ApiClient();
		client.setBasePath(httpApiMockServer.baseUrl());
		StructureApi structureApi = new StructureApi(client);
		Structure structure = structureApi.getStructureById("1");
		assertEquals("0123456789", structure.getPhone());

	}
	
	/**
	 * Upload changes delete PS.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName("Call delete API with return code 200")
	void uploadChangesDeletePS() throws Exception {
		httpApiMockServer.stubFor(delete("/ps/810107592544")
			    .willReturn(aResponse()
			    		.withStatus(200)));
		httpApiMockServer.stubFor(put("/structure")
				.willReturn(aResponse()
						.withStatus(200)));
		//Test
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String rootpath = cl.getResource(".").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		//Day 1 : Generate old ser file
		LoadProcess p = new LoadProcess(new FileExtracted());
		p.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120513.txt").getPath());
		p.getState().setProcess(p);
		p.nextStep();
		p.setState(new ChangesApplied(customMetrics));
		p.getState().setProcess(p);
		p.nextStep();
		// Day 2 : Compute diff (1 delete)
		LoadProcess p2 = new LoadProcess(new FileExtracted());
		registry.register(Integer.toString(registry.nextId()), p2);
		p2.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120514.txt").getPath());
		p2.getState().setProcess(p2);
		p2.nextStep();
		// Day 2 : upload changes (1 delete)
		String[] exclusions = {"90"};
		p2.setState(new UploadingChanges(exclusions, httpApiMockServer.baseUrl()));
		p2.getState().setProcess(p2);
		p2.nextStep();
		assertEquals(0,p2.getPsToCreate().size());
		assertEquals(0,p2.getPsToDelete().size());
		assertEquals(0,p2.getPsToUpdate().size());

	}
	
	/**
	 * Upload changes delete PS 404.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName("Call delete API with return code 404")
	void uploadChangesDeletePS404() throws Exception {
		httpApiMockServer.stubFor(delete("/ps/810107592544")
			    .willReturn(aResponse()
			    		.withStatus(404)));
		httpApiMockServer.stubFor(put("/structure")
				.willReturn(aResponse()
						.withStatus(200)));
		//Test
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String rootpath = cl.getResource(".").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		//Day 1 : Generate old ser file
		LoadProcess p = new LoadProcess(new FileExtracted());
		p.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120513.txt").getPath());
		p.nextStep();
		p.setState(new ChangesApplied(customMetrics));
		p.getState().setProcess(p);
		p.nextStep();
		// Day 2 : Compute diff (1 delete)
		LoadProcess p2 = new LoadProcess(new FileExtracted());
		registry.register(Integer.toString(registry.nextId()), p2);
		p2.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120514.txt").getPath());
		p2.nextStep();
		p2.setState(new DiffComputed(customMetrics));
		p2.nextStep();
		// Day 2 : upload changes (1 delete)
		String[] exclusions = {"90"};
		p2.setState(new UploadingChanges(exclusions, httpApiMockServer.baseUrl()));
		p2.nextStep();
		assertEquals(0,p2.getPsToCreate().size());
		assertEquals(1,p2.getPsToDelete().size());
		assertEquals(0,p2.getPsToUpdate().size());
		assertEquals(HttpStatus.NOT_FOUND.value(),  p2.getPsToDelete().get("810107592544").getReturnStatus());

	}
}
