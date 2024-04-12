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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.metrics.CustomMetrics.SizeMetric;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.service.EmailService;
import fr.ans.psc.pscload.utils.FileUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class DiffComputedStateTest.
 */
@Slf4j
@SpringBootTest
public class DiffComputedStateTest {

    /**
     * The custom metrics.
     */
    @Autowired
    CustomMetrics customMetrics;

    @Autowired
    private EmailService emailService;

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

        httpMockServer.stubFor(any(anyUrl())
                .willReturn(aResponse().withStatus(200)));
    }

    /**
     * Size metrics test.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    @DisplayName("should set size metrics")
    void sizeMetricsTest() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String rootpath = cl.getResource("work").getPath();
        File mapser = new File(rootpath + File.separator + "maps.ser");
        if (mapser.exists()) {
            mapser.delete();
        }
        LoadProcess p = new LoadProcess(new ReadyToComputeDiff(List.of("60"),customMetrics, httpMockServer.baseUrl()));
        File extractFile = FileUtils.copyFileToWorkspace("Extraction_ProSanteConnect_Personne_activite_202112120512.txt");
        p.setExtractedFilename(extractFile.getPath());
        httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps")).withQueryParam("page", equalTo("0"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(410)));
        p.nextStep();

        DiffComputed diffComputed1 = new DiffComputed(customMetrics);
        p.setState(diffComputed1);
        p.nextStep();

        // NO EXISTING SER, REFERENCE METRICS MUST BE STILL SET AT -1
        assertEquals(-1, diffComputed1.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.REFERENCE_ADELI_SIZE).get());
        assertEquals(-1, diffComputed1.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.REFERENCE_RPPS_SIZE).get());

        String[] exclusions = { "90" };
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

        DiffComputed diffComputed2 = new DiffComputed(customMetrics);
        p2.setState(diffComputed2);
        p2.nextStep();

        // DAY 2, REFERENCE METRICS ARE SET ACCORDING TO SERIALIZED FILE FROM DAY 1
        assertEquals(3, diffComputed2.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.REFERENCE_ADELI_SIZE).get());
        assertEquals(2, diffComputed2.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.REFERENCE_RPPS_SIZE).get());

        // DIFFERENCE BETWEEN FILE1 AND FILE2
        assertEquals(0, diffComputed2.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.CREATE_ADELI_SIZE).get());
        assertEquals(1, diffComputed2.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.CREATE_RPPS_SIZE).get());
        assertEquals(0, diffComputed2.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.DELETE_ADELI_SIZE).get());
        assertEquals(1, diffComputed2.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.DELETE_RPPS_SIZE).get());
        assertEquals(1, diffComputed2.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.UPDATE_ADELI_SIZE).get());
        assertEquals(1, diffComputed2.getCustomMetrics().getAppSizeGauges().get(
                SizeMetric.UPDATE_RPPS_SIZE).get());
    }
}
