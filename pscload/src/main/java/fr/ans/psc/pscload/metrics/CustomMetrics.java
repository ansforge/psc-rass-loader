/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.metrics;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import fr.ans.psc.model.Ps;
import fr.ans.psc.model.Structure;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * The Class CustomMetrics.
 */
@Component
public class CustomMetrics {

    private static final String PS_METRIC_NAME = "ps.metric";
    private static final String STRUCTURE_METRIC_NAME = "structure.metric";
    private static final String PROGRESSION_METRIC_NAME = "progression.metric";
    private static final String ID_TYPE_TAG = "idType";
    private static final String OPERATION_TAG = "operation";
    private static final String ENTITY_TAG = "entity";

    /** The Constant SER_FILE_TAG. */
    public static final String SER_FILE_TAG = "ser.file";
    
    /** The Constant TIMESTAMP_TAG. */
    public static final String TIMESTAMP_TAG = "timestamp";

    /**
     * The Enum ID_TYPE.
     */
    public enum ID_TYPE {
        
        /** The adeli. */
        ADELI("0"),
        
        /** The finess. */
        FINESS("3"),
        
        /** The siret. */
        SIRET("5"),
        
        /** The rpps. */
        RPPS("8");

        /** The value. */
        public String value;

        /**
         * Instantiates a new id type.
         *
         * @param value the value
         */
        ID_TYPE(String value) {
            this.value = value;
        }
    }

    /**
     * The Enum OPERATION.
     */
    public enum OPERATION {
        
        /** The create. */
        CREATE,
        
        /** The update. */
        UPDATE,
        
        /** The delete. */
        DELETE,
        
        /** The upload. */
        UPLOAD
    }

    /**
     * The Enum ENTITY_TYPE.
     */
    public enum ENTITY_TYPE {
        
        /** The ps. */
        PS,
        
        /** The structure. */
        STRUCTURE
    }

    private final Map<PsCustomMetric, AtomicInteger> appPsSizeGauges = new EnumMap<>(PsCustomMetric.class);
    private final Map<StructureCustomMetric, AtomicInteger> appStructureSizeGauges = new EnumMap<>(StructureCustomMetric.class);
    private final Map<ProgressionCustomMetric, AtomicInteger> appProgressionGauges = new EnumMap<>(ProgressionCustomMetric.class);
    private final Map<MiscCustomMetric, AtomicInteger> appMiscGauges = new EnumMap<>(MiscCustomMetric.class);


    /**
     * The Enum PsCustomMetric.
     */
    public enum PsCustomMetric {
        
        /** The ps adeli upload size. */
        PS_ADELI_UPLOAD_SIZE,
        
        /** The ps finess upload size. */
        PS_FINESS_UPLOAD_SIZE,
        
        /** The ps siret upload size. */
        PS_SIRET_UPLOAD_SIZE,
        
        /** The ps rpps upload size. */
        PS_RPPS_UPLOAD_SIZE,

        /** The ps adeli delete size. */
        PS_ADELI_DELETE_SIZE,
        
        /** The ps finess delete size. */
        PS_FINESS_DELETE_SIZE,
        
        /** The ps siret delete size. */
        PS_SIRET_DELETE_SIZE,
        
        /** The ps rpps delete size. */
        PS_RPPS_DELETE_SIZE,

        /** The ps adeli create size. */
        PS_ADELI_CREATE_SIZE,
        
        /** The ps finess create size. */
        PS_FINESS_CREATE_SIZE,
        
        /** The ps siret create size. */
        PS_SIRET_CREATE_SIZE,
        
        /** The ps rpps create size. */
        PS_RPPS_CREATE_SIZE,

        /** The ps adeli update size. */
        PS_ADELI_UPDATE_SIZE,
        
        /** The ps finess update size. */
        PS_FINESS_UPDATE_SIZE,
        
        /** The ps siret update size. */
        PS_SIRET_UPDATE_SIZE,
        
        /** The ps rpps update size. */
        PS_RPPS_UPDATE_SIZE,

        /** The ps adeli reference size. */
        PS_ADELI_REFERENCE_SIZE,
        
        /** The ps finess reference size. */
        PS_FINESS_REFERENCE_SIZE,
        
        /** The ps siret reference size. */
        PS_SIRET_REFERENCE_SIZE,
        
        /** The ps rpps reference size. */
        PS_RPPS_REFERENCE_SIZE,

    }

    /**
     * The Enum StructureCustomMetric.
     */
    public enum StructureCustomMetric {
        
        /** The structure upload size. */
        STRUCTURE_UPLOAD_SIZE,
        
        /** The structure delete size. */
        STRUCTURE_DELETE_SIZE,
        
        /** The structure create size. */
        STRUCTURE_CREATE_SIZE,
        
        /** The structure update size. */
        STRUCTURE_UPDATE_SIZE,
    }

    /**
     * The Enum ProgressionCustomMetric.
     */
    public enum ProgressionCustomMetric {
        
        /** The ps upload progression. */
        PS_UPLOAD_PROGRESSION,
        
        /** The ps delete progression. */
        PS_DELETE_PROGRESSION,
        
        /** The ps create progression. */
        PS_CREATE_PROGRESSION,
        
        /** The ps update progression. */
        PS_UPDATE_PROGRESSION,

        /** The structure upload progression. */
        STRUCTURE_UPLOAD_PROGRESSION,
        
        /** The structure delete progression. */
        STRUCTURE_DELETE_PROGRESSION,
        
        /** The structure create progression. */
        STRUCTURE_CREATE_PROGRESSION,
        
        /** The structure update progression. */
        STRUCTURE_UPDATE_PROGRESSION,
    }

    /**
     * The Enum MiscCustomMetric.
     */
    public enum MiscCustomMetric {
        
        /** The stage. */
        STAGE
    }

    /**
     * Instantiates a new custom metrics.
     *
     * @param meterRegistry the meter registry
     */
    public CustomMetrics(MeterRegistry meterRegistry) {
        appMiscGauges.put(MiscCustomMetric.STAGE, meterRegistry.gauge("pscload.stage", new AtomicInteger(0)));

        // Initialization of metrics

        // PS size metrics :
        // initialize metric for each type and operation for a PS

        Arrays.stream(ID_TYPE.values()).forEach(id_type ->
                Arrays.stream(OPERATION.values()).forEach(operation -> {
                    String metricKey = String.join("_", ENTITY_TYPE.PS.name(), id_type.name(), operation.name(), "SIZE");
                    appPsSizeGauges.put(
                            PsCustomMetric.valueOf(metricKey),
                            meterRegistry.gauge(
                                    PS_METRIC_NAME,
                                    Tags.of(ID_TYPE_TAG, id_type.toString(), OPERATION_TAG, operation.name().toLowerCase()),
                                    // set to -1 to discriminate no changes on this operation & entity (-> 0) and not the correct stage (-> -1)
                                    // a -1 value means that we're not at a stage between diff & upload
                                    // a 0 value means that there are no changes (delete, update, etc) for this particular entity after diff
                                    new AtomicInteger(-1)
                            )
                    );
                })
        );

        // Structure size metrics :
        // initialize metric for each operation for a Structure

        Arrays.stream(OPERATION.values()).forEach(operation -> {
            String metricKey = String.join("_", ENTITY_TYPE.STRUCTURE.name(), operation.name(), "SIZE");
                appStructureSizeGauges.put(
                        StructureCustomMetric.valueOf(metricKey),
                        meterRegistry.gauge(
                                STRUCTURE_METRIC_NAME,
                                Tags.of(OPERATION_TAG, operation.name().toLowerCase()),
                                // set to -1 to discriminate no changes on this operation & entity (-> 0) and not the correct stage (-> -1)
                                // a -1 value means that we're not at a stage between diff & upload
                                // a 0 value means that there are no changes (delete, update, etc) for this particular entity after diff
                                new AtomicInteger(-1)
                        )
                );
        });

        // Progression metrics :
        // Initialize metric for each entity type and operation

        Arrays.stream(OPERATION.values()).forEach(operation ->
                Arrays.stream(ENTITY_TYPE.values()).forEach(entity_type -> {
                        String metricKey = String.join("_", entity_type.name(), operation.name(), "PROGRESSION");
                        appProgressionGauges.put(
                                ProgressionCustomMetric.valueOf(metricKey),
                                meterRegistry.gauge(
                                        PROGRESSION_METRIC_NAME,
                                        Tags.of(ENTITY_TAG, entity_type.name(), OPERATION_TAG, operation.name().toLowerCase()),
                                        new AtomicInteger(0)
                                )
                        );
                })
        );

        Counter.builder(SER_FILE_TAG)
                .tags(TIMESTAMP_TAG, "")
                .register(meterRegistry);
    }

    /**
     * Reset size metrics.
     */
    public void resetSizeMetrics() {
        // reset all PsSizeMetrics
        Arrays.stream(CustomMetrics.ID_TYPE.values()).forEach(id_type -> {
            Arrays.stream(CustomMetrics.OPERATION.values()).forEach(operation -> {
                String metricKey = String.join("_", CustomMetrics.ENTITY_TYPE.PS.name(), id_type.name(), operation.name(), "SIZE");
                appPsSizeGauges.get(CustomMetrics.PsCustomMetric.valueOf(metricKey)).set(-1);
            });
        });
        // reset all StructureSizeMetrics
        Arrays.stream(CustomMetrics.OPERATION.values()).forEach(operation -> {
            String metricKey = String.join("_", CustomMetrics.ENTITY_TYPE.STRUCTURE.name(), operation.name(), "SIZE");
            appStructureSizeGauges.get(CustomMetrics.StructureCustomMetric.valueOf(metricKey)).set(-1);
        });
    }

    /**
     * Sets the upload size metrics after deserializing.
     *
     * @param psMap the ps map
     * @param structureMap the structure map
     */
    public void setUploadSizeMetricsAfterDeserializing(Map<String, Ps> psMap, Map<String, Structure> structureMap) {
        appPsSizeGauges.get(PsCustomMetric.PS_ADELI_UPLOAD_SIZE).set(
                Math.toIntExact(psMap.values().stream().filter(professionnel ->
                        ID_TYPE.ADELI.value.equals(professionnel.getIdType())).count()));

        appPsSizeGauges.get(PsCustomMetric.PS_FINESS_UPLOAD_SIZE).set(
                Math.toIntExact(psMap.values().stream().filter(professionnel ->
                        ID_TYPE.FINESS.value.equals(professionnel.getIdType())).count()));

        appPsSizeGauges.get(PsCustomMetric.PS_SIRET_UPLOAD_SIZE).set(
                Math.toIntExact(psMap.values().stream().filter(professionnel ->
                        ID_TYPE.SIRET.value.equals(professionnel.getIdType())).count()));

        appPsSizeGauges.get(PsCustomMetric.PS_RPPS_UPLOAD_SIZE).set(
                Math.toIntExact(psMap.values().stream().filter(professionnel ->
                        ID_TYPE.RPPS.value.equals(professionnel.getIdType())).count()));

        appStructureSizeGauges.get(StructureCustomMetric.STRUCTURE_UPLOAD_SIZE).set(
                structureMap.values().size());
    }

    /**
     * Gets app gauges.
     *
     * @return the app gauges
     */
    public Map<PsCustomMetric, AtomicInteger> getPsSizeGauges() {
        return appPsSizeGauges;
    }
    public Map<StructureCustomMetric, AtomicInteger> getAppStructureSizeGauges() { return appStructureSizeGauges; }
    public Map<ProgressionCustomMetric, AtomicInteger> getAppProgressionGauges() { return appProgressionGauges; }
    public Map<MiscCustomMetric, AtomicInteger> getAppMiscGauges() { return appMiscGauges; }
}
