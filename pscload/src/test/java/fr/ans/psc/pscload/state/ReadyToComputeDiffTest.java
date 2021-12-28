/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.service.EmailService;
import fr.ans.psc.pscload.service.LoadProcess;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class FileExtractedTest.
 */
@Slf4j
@SpringBootTest
class ReadyToComputeDiffTest {

	@Autowired
	CustomMetrics customMetrics;
	
	@Autowired
	private EmailService emailService;

	@Mock
	private JavaMailSender javaMailSender;

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

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this).close();
		emailService.setEmailSender(javaMailSender);
		File outputfolder = new File(Thread.currentThread().getContextClassLoader().getResource("work").getPath());
		File[] files = outputfolder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				f.delete();
			}
		}

		httpMockServer.stubFor(any(urlMatching("/generate-extract"))
				.willReturn(aResponse().withStatus(200)));
	}

	/**
	 * Initial diff task test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName("Initial diff with no old ser file and 5 ps")
	void initialDiffTaskTest() throws Exception {
		String rootpath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff());
		p.setExtractedFilename(Thread.currentThread().getContextClassLoader()
				.getResource("Extraction_ProSanteConnect_Personne_activite_202112120512.txt").getPath());
		p.nextStep();
		assertEquals(5, p.getPsToCreate().size());
		assertEquals(0, p.getPsToDelete().size());
	}

	/**
	 * Diff task test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName(" diff with 1 supp, 2 modifs and 1 add")
	void diffTaskTest() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String rootpath = cl.getResource(".").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff());
		p.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120512.txt").getPath());
		p.nextStep();
		p.setState(new ChangesApplied(customMetrics, httpMockServer.baseUrl()));
		p.getState().setProcess(p);
		p.nextStep();

		LoadProcess p2 = new LoadProcess(new ReadyToComputeDiff());
		p2.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120515.txt").getPath());
		p2.getState().setProcess(p2);
		p2.nextStep();
		assertEquals(1,p2.getPsToDelete().size());
		assertEquals(1,p2.getPsToCreate().size());
		assertEquals(2, p2.getPsToUpdate().size());
	}

	/**
	 * Diff from large file.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName("initial diff from large file (100000 lines)")
	public void diffFromLargeFile() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String rootpath = cl.getResource(".").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		LoadProcess p = new LoadProcess(new ReadyToComputeDiff());
		p.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112140852.txt").getPath());
		p.nextStep();
		assertEquals(p.getPsToCreate().size(), 99171);
		assertEquals(p.getStructureToCreate().size(), 37534);
	}
}
