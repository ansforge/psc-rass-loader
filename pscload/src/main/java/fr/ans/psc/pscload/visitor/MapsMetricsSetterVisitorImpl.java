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

@Slf4j
public class MapsMetricsSetterVisitorImpl implements MapsVisitor {

    private CustomMetrics customMetrics;

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
