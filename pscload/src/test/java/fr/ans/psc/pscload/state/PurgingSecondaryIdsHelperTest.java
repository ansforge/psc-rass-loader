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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.ans.psc.model.AlternativeIdentifier;
import fr.ans.psc.model.Ps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the static helpers of {@link PurgingSecondaryIds}.
 * These tests do not require Spring context — they cover the pure logic used by
 * the Phase 2 purge: fused detection, origine resolution, UUID matching.
 */
class PurgingSecondaryIdsHelperTest {

    private static final String UUID_PSI = "019ce28d-aa83-7c4b-b7bf-afc3ef900cf8";
    private static final String RPPS = "810012345678";
    private static final String ADELI = "012345678";
    private static final String SIRET = "518751275100038/0000000572";

    private static AlternativeIdentifier alt(String id, String origine) {
        AlternativeIdentifier a = new AlternativeIdentifier();
        a.setIdentifier(id);
        a.setOrigine(origine);
        return a;
    }

    @Nested
    @DisplayName("isFused")
    class IsFusedTests {

        @Test
        @DisplayName("standalone account (ids = [nationalId]) is not fused")
        void standalone() {
            Ps ps = new Ps();
            ps.setNationalId(RPPS);
            ps.setIds(List.of(RPPS));
            assertFalse(PurgingSecondaryIds.isFused(ps));
        }

        @Test
        @DisplayName("null or empty ids is not fused")
        void nullOrEmptyIds() {
            Ps ps = new Ps();
            ps.setNationalId(RPPS);
            ps.setIds(null);
            assertFalse(PurgingSecondaryIds.isFused(ps));

            ps.setIds(Collections.emptyList());
            assertFalse(PurgingSecondaryIds.isFused(ps));
        }

        @Test
        @DisplayName("account with a non-UUID secondary id is fused")
        void fusedWithNonUuid() {
            Ps ps = new Ps();
            ps.setNationalId(UUID_PSI);
            ps.setIds(Arrays.asList(UUID_PSI, RPPS));
            assertTrue(PurgingSecondaryIds.isFused(ps));
        }

        @Test
        @DisplayName("account whose only extra ids are UUIDs is NOT fused (nothing to purge)")
        void onlyUuidSecondaries() {
            Ps ps = new Ps();
            ps.setNationalId(UUID_PSI);
            ps.setIds(Arrays.asList(UUID_PSI, "550e8400-e29b-41d4-a716-446655440000"));
            assertFalse(PurgingSecondaryIds.isFused(ps));
        }
    }

    @Nested
    @DisplayName("getOrigine")
    class GetOrigineTests {

        @Test
        @DisplayName("returns origine from alternativeIds when present")
        void fromAlternativeIds() {
            Ps ps = new Ps();
            ps.setAlternativeIds(Arrays.asList(
                    alt(RPPS, "RPPS"),
                    alt(ADELI, "ADELI")));
            assertEquals("RPPS", PurgingSecondaryIds.getOrigine(ps, RPPS));
            assertEquals("ADELI", PurgingSecondaryIds.getOrigine(ps, ADELI));
        }

        @Test
        @DisplayName("falls back to id-prefix derivation when alternativeIds is missing")
        void fallbackDerivation() {
            Ps ps = new Ps();
            ps.setAlternativeIds(Collections.emptyList());
            assertEquals("RPPS", PurgingSecondaryIds.getOrigine(ps, RPPS));
            assertEquals("ADELI", PurgingSecondaryIds.getOrigine(ps, ADELI));
            assertEquals("SIRET", PurgingSecondaryIds.getOrigine(ps, "5" + SIRET.substring(1)));
            assertEquals("FINESS", PurgingSecondaryIds.getOrigine(ps, "3" + RPPS.substring(1)));
            assertEquals("CAB_RPPS", PurgingSecondaryIds.getOrigine(ps, "6" + RPPS.substring(1)));
        }

        @Test
        @DisplayName("returns null for unknown prefix or null id")
        void unknownReturnsNull() {
            Ps ps = new Ps();
            ps.setAlternativeIds(Collections.emptyList());
            assertNull(PurgingSecondaryIds.getOrigine(ps, null));
            assertNull(PurgingSecondaryIds.getOrigine(ps, "9zzzz"));
        }
    }

    @Nested
    @DisplayName("isUUID")
    class IsUuidTests {

        @Test
        @DisplayName("matches canonical UUID v4/v7")
        void matchesUuid() {
            assertTrue(PurgingSecondaryIds.isUUID("550e8400-e29b-41d4-a716-446655440000"));
            assertTrue(PurgingSecondaryIds.isUUID(UUID_PSI));
        }

        @Test
        @DisplayName("rejects RASS identifiers and malformed strings")
        void rejectsOthers() {
            assertFalse(PurgingSecondaryIds.isUUID(null));
            assertFalse(PurgingSecondaryIds.isUUID(""));
            assertFalse(PurgingSecondaryIds.isUUID(RPPS));
            assertFalse(PurgingSecondaryIds.isUUID("not-a-uuid"));
        }
    }
}
