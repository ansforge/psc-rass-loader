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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Assertions;
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
import fr.ans.psc.pscload.component.Scheduler;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.exception.DownloadException;
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

	/** The http rass mock server. */
	@RegisterExtension
	static WireMockExtension httpRassMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock")).configureStaticDsl(true)
			.build();

	/** The http api mock server. */
	@RegisterExtension
	static WireMockExtension httpApiMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock/api")).configureStaticDsl(true)
			.build();

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
		propertiesRegistry.add("extract.download.url", () -> httpRassMockServer.baseUrl());
		propertiesRegistry.add("files.directory",
				() -> Thread.currentThread().getContextClassLoader().getResource(".").getPath());
		propertiesRegistry.add("api.base.url", () -> httpApiMockServer.baseUrl());
		propertiesRegistry.add("use.ssl", () -> "false");
		propertiesRegistry.add("enable.scheduler", () -> "true");
	}

	@Test
	@DisplayName("Scheduler Error Download test")
	void schedulerTest() throws Exception {
		httpApiMockServer.stubFor(delete("/ps/810107592544").willReturn(aResponse().withStatus(200)));
		httpApiMockServer.stubFor(put("/structure").willReturn(aResponse().withStatus(200)));
		// Configure the mock service to serve zipfile
		String contextPath = "/V300/services/extraction/Extraction_ProSanteConnect";
		String filename = "Extraction_ProSanteConnect_Personne_activite_202112090858.txt";
		zipFile("wiremock/" + filename);
		String path = Thread.currentThread().getContextClassLoader().getResource("wiremock/" + filename + ".zip")
				.getPath();
		byte[] content = readFileToBytes(path);
		httpRassMockServer.stubFor(get(contextPath).willReturn(aResponse().withStatus(200)
				.withHeader("Content-Type", "application/zip")
				.withHeader("Content-Disposition", "attachment; filename=" + filename + ".zip").withBody(content)));
			scheduler.run();
			assertTrue(registry.isEmpty());
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

}
