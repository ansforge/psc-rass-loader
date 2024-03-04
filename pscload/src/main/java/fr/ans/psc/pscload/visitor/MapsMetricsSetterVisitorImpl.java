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
package fr.ans.psc.pscload.visitor;

import java.util.Arrays;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.metrics.CustomMetrics.SizeMetric;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.model.operations.PsCreateMap;
import fr.ans.psc.pscload.model.operations.PsDeleteMap;
import fr.ans.psc.pscload.model.operations.PsUpdateMap;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class MapsMetricsSetterVisitorImpl.
 */
@Slf4j
public class MapsMetricsSetterVisitorImpl implements MapsVisitor {

    private CustomMetrics customMetrics;

    /**
     * Instantiates a new maps metrics setter visitor impl.
     *
     * @param customMetrics the custom metrics
     */
    public MapsMetricsSetterVisitorImpl(CustomMetrics customMetrics) {
        this.customMetrics = customMetrics;
    }

    @Override
    public void visit(PsCreateMap map) {
        setPsMetricFromPsMap(map);
    }

    @Override
    public void visit(PsDeleteMap map) {
        setPsMetricFromPsMap(map);
    }

    @Override
    public void visit(PsUpdateMap map) {
        setPsMetricFromPsMap(map);
    }


    private void setPsMetricFromPsMap(OperationMap<String, RassEntity> map) {
        Arrays.stream(CustomMetrics.ID_TYPE.values()).forEach(id_type -> {
            String metricKey = String.join("_", map.getOperation().name(), id_type.name(), "SIZE");
            SizeMetric metric = CustomMetrics.SizeMetric.valueOf(metricKey);

            customMetrics.setPsMetricSize(
                    metric,
                    Math.toIntExact(map.values().stream().filter(item -> item.getIdType().equals(id_type.value)).count())
            );
            log.info("{} --- {}", metricKey, customMetrics.getAppSizeGauges().get(metric).get());
        });
    }
}
