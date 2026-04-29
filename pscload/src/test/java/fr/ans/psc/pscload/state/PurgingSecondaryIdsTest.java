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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import fr.ans.psc.pscload.model.LoadProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Integration test for {@link PurgingSecondaryIds} (Phase 2, E01-U005).
 *
 * Covers acceptance criteria :
 * <ul>
 *   <li><b>CA04</b> : non-PSI account fused with a non-PSI secondary that disappears → purge the secondary,
 *       account stays active.</li>
 *   <li><b>CA05</b> : PSI account fused with RPPS(B), ADELI(C), CAB_RPPS(D); B disappears → purge B,
 *       then cascade RPPS→ADELI removes C. D (CAB_RPPS) is preserved.</li>
 *   <li><b>Cascade non déclenchée</b> : PSI with two RPPS + one ADELI, only one RPPS disappears →
 *       the other RPPS remains, ADELI is NOT cascaded.</li>
 *   <li><b>ADELI secondaire disparu seul</b> : fused account where an ADELI secondary disappears →
 *       nothing happens (ADELI pre-filter, schema RG01).</li>
 *   <li><b>Standalone</b> : account with a single id → skipped.</li>
 * </ul>
 */
@Slf4j
class PurgingSecondaryIdsTest {

    // ─── Test data: identifiers chosen with clear idType prefixes ────────────────
    private static final String PSI_UUID_1 = "019ce28d-aa83-7c4b-b7bf-afc3ef900cf8";
    private static final String PSI_UUID_2 = "019ce28d-aa83-7c4b-b7bf-afc3ef900cf9";

    // CA04 : non-PSI (A) + non-PSI secondary (B) ; B disappears
    private static final String CA04_A_RPPS = "810000000100";
    private static final String CA04_B_SIRET = "510000000200";

    // CA05 : PSI (A) + RPPS (B) + ADELI (C) + CAB_RPPS (D) ; only B disappears
    private static final String CA05_B_RPPS = "810000000300";
    private static final String CA05_C_ADELI = "010000000400";
    private static final String CA05_D_CABRPPS = "610000000500";

    // Cascade non-déclenchée : PSI + RPPS_1 + RPPS_2 + ADELI ; only RPPS_1 disappears
    private static final String NOCASCADE_B1_RPPS = "810000000600";
    private static final String NOCASCADE_B2_RPPS = "810000000700";
    private static final String NOCASCADE_C_ADELI = "010000000800";

    // ADELI only scenario : fused account with an ADELI secondary that disappears
    private static final String ADELIONLY_A_RPPS = "810000000900";
    private static final String ADELIONLY_C_ADELI = "010000001000";

    // Standalone account (no secondary)
    private static final String STANDALONE_A_RPPS = "810000001100";

    @RegisterExtension
    static WireMockExtension apiMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private Path tmpDir;
    private File rassFile;

    @BeforeEach
    void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("pscload-purgetest");
        rassFile = tmpDir.resolve("rass.txt").toFile();

        // RASS file : only CA04_A_RPPS, CA05_D_CABRPPS, NOCASCADE_B2_RPPS, NOCASCADE_C_ADELI,
        //             ADELIONLY_A_RPPS and STANDALONE_A_RPPS are present.
        // Notably MISSING from RASS : CA04_B_SIRET, CA05_B_RPPS, CA05_C_ADELI, NOCASCADE_B1_RPPS,
        //                            ADELIONLY_C_ADELI.
        writeRassFile(
                CA04_A_RPPS,
                CA05_D_CABRPPS,
                NOCASCADE_B2_RPPS,
                NOCASCADE_C_ADELI,
                ADELIONLY_A_RPPS,
                STANDALONE_A_RPPS);

        // Page 0 returns our 5 test accounts, page 1 returns 410 (end of pagination).
        apiMock.stubFor(get(urlPathEqualTo("/v2/ps"))
                .withQueryParam("page", equalTo("0"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(buildPage0Json())));
        apiMock.stubFor(get(urlPathEqualTo("/v2/ps"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse().withStatus(410)));

        // All DELETE source-profession calls succeed (204).
        apiMock.stubFor(delete(urlMatching("/v2/ps/.+/source/.+"))
                .willReturn(aResponse().withStatus(204)));
    }

    @AfterEach
    void tearDown() throws IOException {
        if (rassFile.exists()) rassFile.delete();
        Files.deleteIfExists(tmpDir);
    }

    @Test
    @DisplayName("Phase 2 : CA04 + CA05 + cascade non-déclenchée + ADELI pré-filtré + standalone")
    void phase2_fullCoverage() {
        PurgingSecondaryIds state = new PurgingSecondaryIds(apiMock.baseUrl());
        LoadProcess process = new LoadProcess(state);
        process.setExtractedFilename(rassFile.getAbsolutePath());
        state.setProcess(process);

        process.nextStep();

        // ─── CA04 : DELETE B_SIRET from A_RPPS ───────────────────────────────
        apiMock.verify(1, deleteRequestedFor(urlMatching(
                "/v2/ps/" + CA04_A_RPPS + "/source/" + CA04_B_SIRET)));

        // ─── CA05 : direct purge B_RPPS, cascade ADELI C, D preserved ────────
        apiMock.verify(1, deleteRequestedFor(urlMatching(
                "/v2/ps/" + PSI_UUID_1 + "/source/" + CA05_B_RPPS)));
        apiMock.verify(1, deleteRequestedFor(urlMatching(
                "/v2/ps/" + PSI_UUID_1 + "/source/" + CA05_C_ADELI)));
        apiMock.verify(0, deleteRequestedFor(urlMatching(
                "/v2/ps/" + PSI_UUID_1 + "/source/" + CA05_D_CABRPPS)));

        // ─── Cascade non déclenchée : B1 disparaît, B2 reste → ADELI préservé ─
        apiMock.verify(1, deleteRequestedFor(urlMatching(
                "/v2/ps/" + PSI_UUID_2 + "/source/" + NOCASCADE_B1_RPPS)));
        apiMock.verify(0, deleteRequestedFor(urlMatching(
                "/v2/ps/" + PSI_UUID_2 + "/source/" + NOCASCADE_B2_RPPS)));
        apiMock.verify(0, deleteRequestedFor(urlMatching(
                "/v2/ps/" + PSI_UUID_2 + "/source/" + NOCASCADE_C_ADELI)));

        // ─── ADELI secondaire disparu seul : aucun appel (pré-filtre schéma) ──
        apiMock.verify(0, deleteRequestedFor(urlMatching(
                "/v2/ps/" + ADELIONLY_A_RPPS + "/source/" + ADELIONLY_C_ADELI)));

        // ─── Standalone : aucun appel ─────────────────────────────────────────
        apiMock.verify(0, deleteRequestedFor(urlMatching(
                "/v2/ps/" + STANDALONE_A_RPPS + "/source/.+")));
    }

    // ─── Test fixtures helpers ──────────────────────────────────────────────────

    /** RASS file : header + one data line per nationalId, all other columns empty. */
    private void writeRassFile(String... nationalIds) throws IOException {
        StringBuilder sb = new StringBuilder();
        // 50 columns header (only the first 3 matter : idType | id | nationalId).
        sb.append("Type d'identifiant PP|Identifiant PP|Identification nationale PP");
        for (int i = 3; i < 50; i++) sb.append("|col").append(i);
        sb.append('\n');

        for (String id : nationalIds) {
            sb.append("8|").append(id).append('|').append(id);
            for (int i = 3; i < 50; i++) sb.append('|');
            sb.append('\n');
        }
        Files.writeString(rassFile.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }

    /** JSON array of Ps for page 0 — 5 fused accounts covering all scenarios. */
    private String buildPage0Json() {
        StringBuilder sb = new StringBuilder("[");
        sb.append(ps(CA04_A_RPPS,
                new String[]{CA04_A_RPPS, CA04_B_SIRET},
                new String[][]{{CA04_A_RPPS, "RPPS"}, {CA04_B_SIRET, "SIRET"}}));
        sb.append(",");
        sb.append(ps(PSI_UUID_1,
                new String[]{PSI_UUID_1, CA05_B_RPPS, CA05_C_ADELI, CA05_D_CABRPPS},
                new String[][]{
                        {PSI_UUID_1, "PSI"},
                        {CA05_B_RPPS, "RPPS"},
                        {CA05_C_ADELI, "ADELI"},
                        {CA05_D_CABRPPS, "CAB_RPPS"}}));
        sb.append(",");
        sb.append(ps(PSI_UUID_2,
                new String[]{PSI_UUID_2, NOCASCADE_B1_RPPS, NOCASCADE_B2_RPPS, NOCASCADE_C_ADELI},
                new String[][]{
                        {PSI_UUID_2, "PSI"},
                        {NOCASCADE_B1_RPPS, "RPPS"},
                        {NOCASCADE_B2_RPPS, "RPPS"},
                        {NOCASCADE_C_ADELI, "ADELI"}}));
        sb.append(",");
        sb.append(ps(ADELIONLY_A_RPPS,
                new String[]{ADELIONLY_A_RPPS, ADELIONLY_C_ADELI},
                new String[][]{{ADELIONLY_A_RPPS, "RPPS"}, {ADELIONLY_C_ADELI, "ADELI"}}));
        sb.append(",");
        sb.append(ps(STANDALONE_A_RPPS,
                new String[]{STANDALONE_A_RPPS},
                new String[][]{{STANDALONE_A_RPPS, "RPPS"}}));
        sb.append("]");
        return sb.toString();
    }

    /** Minimal Ps JSON (only the fields Phase 2 reads). */
    private String ps(String nationalId, String[] ids, String[][] altIds) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"nationalId\":\"").append(nationalId).append("\",");
        sb.append("\"idType\":\"8\",");
        sb.append("\"ids\":[");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(ids[i]).append("\"");
        }
        sb.append("],");
        sb.append("\"alternativeIds\":[");
        for (int i = 0; i < altIds.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"identifier\":\"").append(altIds[i][0]).append("\",");
            sb.append("\"origine\":\"").append(altIds[i][1]).append("\",");
            sb.append("\"quality\":1}");
        }
        sb.append("],\"professions\":[]}");
        return sb.toString();
    }
}
