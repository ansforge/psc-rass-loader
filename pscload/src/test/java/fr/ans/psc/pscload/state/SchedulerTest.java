/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.component.Scheduler;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class DiffComputedStateTest.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
@AutoConfigureMockMvc
public class SchedulerTest {

	@Autowired
	private ProcessRegistry registry;

	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private MockMvc mockmvc;

	/** The http rass mock server. */
	@RegisterExtension
	static WireMockExtension httpRassMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock")).configureStaticDsl(true)
			.build();

	/** The http api mock server. */
	@RegisterExtension
	static WireMockExtension httpApiMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig()
					.dynamicPort()
					.usingFilesUnderClasspath("wiremock/api"))
			.configureStaticDsl(true)
			.build();

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
		propertiesRegistry.add("extract.download.url",
				() -> httpRassMockServer.baseUrl() + "/V300/services/extraction/Extraction_ProSanteConnect");
		propertiesRegistry.add("files.directory",
				() -> Thread.currentThread().getContextClassLoader().getResource("work").getPath());
		propertiesRegistry.add("api.base.url", () -> httpApiMockServer.baseUrl());
		propertiesRegistry.add("use.x509.auth", () -> "false");
		propertiesRegistry.add("enable.scheduler", () -> "true");
	}

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
	}

	@Test
	@DisplayName("Scheduler Error Download test (bad url)")
	void schedulerTest() throws Exception {
		httpApiMockServer.stubFor(delete("/ps/810107592544").willReturn(aResponse().withStatus(200)));
		httpApiMockServer.stubFor(put("/structure").willReturn(aResponse().withStatus(200)));
		// Configure the mock service to serve zipfile
		String contextPath = "/V300/services/extraction/Extraction_ProSanteConnect";
		httpRassMockServer.stubFor(get(contextPath).willReturn(aResponse().withStatus(404)));
		scheduler.run();
		assertTrue(registry.isEmpty());
	}

	@Test
	@DisplayName("Scheduler end to end test")
	void schedulerNominalProccessTest() throws Exception {
		// Configure the mock service to serve zipfile
		String contextPath = "/V300/services/extraction/Extraction_ProSanteConnect";
		String filename = "Extraction_ProSanteConnect_Personne_activite_202112090858.txt";
		zipFile("wiremock/" + filename);
		String path = Thread.currentThread().getContextClassLoader().getResource("wiremock/" + filename + ".zip")
				.getPath();
		byte[] content = readFileToBytes(path);
		httpApiMockServer.stubFor(any(urlMatching("/.*")).willReturn(aResponse().withStatus(200)));
		httpRassMockServer.stubFor(get(contextPath).willReturn(aResponse().withStatus(200)
				.withHeader("Content-Type", "application/zip")
				.withHeader("Content-Disposition", "attachment; filename=" + filename + ".zip").withBody(content)));
		scheduler.run();
		//TODO fix problem with async request of controller(wiremock is stopped before end of test
		mockmvc.perform(post("/process/sync-continue").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	private static void zipFile(String filename) throws Exception {

		String filePath = Thread.currentThread().getContextClassLoader().getResource(filename).getPath();
		File file = new File(filePath);
		String zipFileName = file.getPath().concat(".zip");

		FileOutputStream fos = new FileOutputStream(zipFileName);
		ZipOutputStream zos = new ZipOutputStream(fos);

		zos.putNextEntry(new ZipEntry(file.getName()));

		byte[] bytes = readFileToBytes(filePath);
		zos.write(bytes, 0, bytes.length);
		zos.closeEntry();
		zos.close();
	}

	private static byte[] readFileToBytes(String filePath) throws IOException {

		File file = new File(filePath);
		byte[] bytes = new byte[(int) file.length()];

		// funny, if can use Java 7, please uses Files.readAllBytes(path)
		try (FileInputStream fis = new FileInputStream(file)) {
			fis.read(bytes);
		}
		return bytes;
	}

}
