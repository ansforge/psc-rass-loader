package fr.ans.psc.pscload.state;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
		System.out.println(httpApiMockServer.getOptions().filesRoot().getPath());
		ApiClient client = new ApiClient();
		client.setBasePath(httpApiMockServer.baseUrl());
		StructureApi structureApi = new StructureApi(client);
		Structure structure = structureApi.getStructureById("1");
		assertEquals("0123456789", structure.getPhone());

	}
	
	
}
