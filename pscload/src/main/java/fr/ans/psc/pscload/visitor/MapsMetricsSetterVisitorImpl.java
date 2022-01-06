package fr.ans.psc.pscload.visitor;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.operations.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapsMetricsSetterVisitorImpl implements MapsVisitor {

    private CustomMetrics customMetrics;

    public MapsMetricsSetterVisitorImpl(CustomMetrics customMetrics) {
        this.customMetrics = customMetrics;
    }

    @Override
    public void visit(PsCreateMap map) {
        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_ADELI_CREATE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.ADELI.value))
                        .count())
        );

        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_FINESS_CREATE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.FINESS.value))
                        .count())
        );

        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_SIRET_CREATE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.SIRET.value))
                        .count())
        );

        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_RPPS_CREATE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.RPPS.value))
                        .count())
        );
    }

    @Override
    public void visit(PsDeleteMap map) {
        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_ADELI_DELETE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.ADELI.value))
                        .count())
        );

        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_FINESS_DELETE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.FINESS.value))
                        .count())
        );

        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_SIRET_DELETE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.SIRET.value))
                        .count())
        );

        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_RPPS_DELETE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.RPPS.value))
                        .count())
        );
    }

    @Override
    public void visit(PsUpdateMap map) {
        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_ADELI_UPDATE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.ADELI.value))
                        .count())
        );

        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_FINESS_UPDATE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.FINESS.value))
                        .count())
        );

        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_SIRET_UPDATE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.SIRET.value))
                        .count())
        );

        customMetrics.setPsMetricSize(
                CustomMetrics.PsCustomMetric.PS_RPPS_UPDATE_SIZE,
                Math.toIntExact(map.values().stream()
                        .filter(item -> item.getIdType().equals(CustomMetrics.ID_TYPE.RPPS.value))
                        .count())
        );
    }

    @Override
    public void visit(StructureCreateMap map) {
        customMetrics.setStructureMetricSize(
                CustomMetrics.StructureCustomMetric.STRUCTURE_CREATE_SIZE,
                map.values().size()
        );
    }

    @Override
    public void visit(StructureUpdateMap map) {
        customMetrics.setStructureMetricSize(
                CustomMetrics.StructureCustomMetric.STRUCTURE_UPDATE_SIZE,
                map.values().size()
        );
    }
}
