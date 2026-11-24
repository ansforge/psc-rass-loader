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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.metrics.CustomMetrics.SizeMetric;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.model.operations.OperationType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Diff size metrics must be published at DiffComputed, so that AlertManager
 * thresholds are evaluated on the current diff and not on the effective counts
 * left by the previous run.
 */
class DiffComputedMetricsTest {

    private static Professionnel ps(String idType, String nationalId) {
        Professionnel ps = new Professionnel();
        ps.setIdType(idType);
        ps.setNationalId(nationalId);
        return ps;
    }

    private static OperationMap<String, RassEntity> map(LoadProcess p, OperationType op) {
        return p.getMaps().stream().filter(m -> m.getOperation() == op).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("DiffComputed publishes CREATE/UPDATE/DELETE sizes per id type, overriding previous run values")
    void publishesDiffMetrics() {
        CustomMetrics metrics = new CustomMetrics(new SimpleMeterRegistry());
        // values left by the previous run (effective counts published by ChangesApplied)
        metrics.setPsMetricSize(SizeMetric.UPDATE_RPPS_SIZE, 1048293);
        metrics.setPsMetricSize(SizeMetric.CREATE_FINESS_SIZE, 229);

        DiffComputed state = new DiffComputed(metrics);
        LoadProcess p = new LoadProcess(state);
        map(p, OperationType.UPDATE).put("810000000001", ps("8", "810000000001"));
        map(p, OperationType.UPDATE).put("810000000002", ps("8", "810000000002"));
        map(p, OperationType.UPDATE).put("3123456789/01", ps("3", "3123456789/01"));
        map(p, OperationType.CREATE).put("512345678900012/01", ps("5", "512345678900012/01"));
        map(p, OperationType.DELETE).put("810000000003", ps("8", "810000000003"));

        state.nextStep();

        assertEquals(2, metrics.getAppSizeGauges().get(SizeMetric.UPDATE_RPPS_SIZE).get());
        assertEquals(1, metrics.getAppSizeGauges().get(SizeMetric.UPDATE_FINESS_SIZE).get());
        assertEquals(0, metrics.getAppSizeGauges().get(SizeMetric.UPDATE_SIRET_SIZE).get());
        assertEquals(1, metrics.getAppSizeGauges().get(SizeMetric.CREATE_SIRET_SIZE).get());
        assertEquals(0, metrics.getAppSizeGauges().get(SizeMetric.CREATE_FINESS_SIZE).get());
        assertEquals(1, metrics.getAppSizeGauges().get(SizeMetric.DELETE_RPPS_SIZE).get());
        assertEquals(0, metrics.getAppSizeGauges().get(SizeMetric.DELETE_ADELI_SIZE).get());
        // REFERENCE is not touched by DiffComputed
        assertEquals(-1, metrics.getAppSizeGauges().get(SizeMetric.REFERENCE_RPPS_SIZE).get());
    }
}
