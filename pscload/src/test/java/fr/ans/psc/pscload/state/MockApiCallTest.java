package fr.ans.psc.pscload.state;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.StructureApi;
import fr.ans.psc.model.Structure;
import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.service.LoadProcess;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
public class MockApiCallTest {

	@RegisterExtension
	static WireMockExtension httpApiMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig()
					.dynamicPort()
					.usingFilesUnderClasspath("wiremock/api"))
			.configureStaticDsl(true).build();

	
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
		p.runtask();
		// Day 2 : Compute diff (1 delete)
		LoadProcess p2 = new LoadProcess(new FileExtracted());
		p2.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120514.txt").getPath());
		p2.runtask();
		// Day 2 : upload changes (1 delete)
		String[] excludedProfessions = {"90"};
		p2.setState(new DiffComputed(excludedProfessions, httpApiMockServer.baseUrl() ));
		p2.runtask();
		//TODO add assertions (not possible to check the size of maps because they are unmodifiables

	}
	
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
		p.runtask();
		// Day 2 : Compute diff (1 delete)
		LoadProcess p2 = new LoadProcess(new FileExtracted());
		p2.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120514.txt").getPath());
		p2.runtask();
		// Day 2 : upload changes (1 delete)
		String[] excludedProfessions = {"90"};
		p2.setState(new DiffComputed(excludedProfessions, httpApiMockServer.baseUrl() ));
		p2.runtask();
		//TODO add assertions (not possible to check the size of maps because they are unmodifiables

	}
}
