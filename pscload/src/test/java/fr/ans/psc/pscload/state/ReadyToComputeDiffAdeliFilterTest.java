/*
 * Copyright © 2022-2026 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package fr.ans.psc.pscload.state;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.model.operations.OperationType;
import fr.ans.psc.pscload.service.EmailService;
import fr.ans.psc.pscload.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Verifies the schema RG01 pre-filter : a PS of idType ADELI ("0") whose nationalId
 * is in DB but absent from the RASS file must NOT appear in {@code PsDeleteMap}.
 * The actual cleanup of orphaned ADELI ids (when their parent RPPS disappears) is
 * covered in {@link PurgingSecondaryIdsTest} (Phase 2 cascade).
 */
@Slf4j
@SpringBootTest
class ReadyToComputeDiffAdeliFilterTest {

    @Autowired
    private CustomMetrics customMetrics;

    @Autowired
    private EmailService emailService;

    @RegisterExtension
    static WireMockExtension httpMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("extract.download.url",
                () -> httpMockServer.baseUrl() + "/V300/services/extraction/Extraction_ProSanteConnect");
        registry.add("files.directory",
                () -> Thread.currentThread().getContextClassLoader().getResource("work").getPath());
        registry.add("api.base.url", () -> httpMockServer.baseUrl());
        registry.add("use.x509.auth", () -> "false");
        registry.add("enable.scheduler", () -> "true");
        registry.add("scheduler.cron", () -> "0 0 1 15 * ?");
        registry.add("pscextract.base.url", () -> httpMockServer.baseUrl());
    }

    @BeforeEach
    void setUp() {
        File outputfolder = new File(
                Thread.currentThread().getContextClassLoader().getResource("work").getPath());
        File[] files = outputfolder.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        httpMockServer.stubFor(any(anyUrl()).willReturn(aResponse().withStatus(200)));
    }

    @Test
    @DisplayName("RG01 pre-filter : an ADELI absent from RASS n'est pas dans PsDeleteMap")
    void adeliMissingFromRass_isFilteredFromDeleteMap() throws Exception {
        LoadProcess p = new LoadProcess(new ReadyToComputeDiff(
                List.of("60"), customMetrics, httpMockServer.baseUrl()));
        File extractFile = FileUtils.copyFileToWorkspace(
                "Extraction_ProSanteConnect_Personne_activite_202112120515.txt");
        p.setExtractedFilename(extractFile.getPath());
        p.getState().setProcess(p);

        // DB page 0 : 2 entrées — une ADELI "0099999999" (non présente dans RASS) et
        // une RPPS "810099999999" (non présente dans RASS non plus).
        // Attendu : la RPPS part en DELETE, l'ADELI est filtrée par le pré-filtre RG01.
        String dbJson = "[" + ps("0", "0099999999") + "," + ps("8", "810099999999") + "]";
        httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps"))
                .withQueryParam("page", equalTo("0"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(dbJson)));
        httpMockServer.stubFor(get(urlPathEqualTo("/v2/ps"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse().withStatus(410)));

        p.nextStep();

        OperationMap<String, RassEntity> psToDelete = p.getMaps().stream()
                .filter(map -> map.getOperation().equals(OperationType.DELETE))
                .findFirst().get();

        // L'ADELI "0099999999" ne doit PAS être dans PsDeleteMap (pré-filtre).
        assertFalse(psToDelete.containsKey("0099999999"),
                "ADELI 0099999999 ne doit pas être dans PsDeleteMap (schema RG01)");
        // La RPPS "810099999999" doit rester dans PsDeleteMap (comportement nominal).
        assertTrue(psToDelete.containsKey("810099999999"),
                "RPPS 810099999999 doit être dans PsDeleteMap");
        assertEquals(1, psToDelete.size(),
                "PsDeleteMap doit contenir exactement 1 entrée (la RPPS, pas l'ADELI)");
    }

    /** Minimal Ps JSON (only the fields the differ/loadMap needs). */
    private String ps(String idType, String nationalId) {
        return "{"
                + "\"idType\":\"" + idType + "\","
                + "\"id\":\"" + nationalId + "\","
                + "\"nationalId\":\"" + nationalId + "\","
                + "\"lastName\":\"TEST\","
                + "\"firstNames\":[],"
                + "\"professions\":[],"
                + "\"ids\":[\"" + nationalId + "\"],"
                + "\"alternativeIds\":[],"
                + "\"activated\":1,"
                + "\"deactivated\":null"
                + "}";
    }
}
