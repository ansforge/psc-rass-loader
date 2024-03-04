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
package fr.ans.psc.pscload.metrics;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import fr.ans.psc.pscload.model.Stage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * The Class CustomMetrics.
 */
@Component
public class CustomMetrics {

	private MeterRegistry meterRegistry;
	private static final String PS_METRIC_NAME = "ps.metric";
	private static final String ID_TYPE_TAG = "idType";
	private static final String OPERATION_TAG = "operation";

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
		REFERENCE
	}

	private final Map<SizeMetric, AtomicInteger> appSizeGauges = new EnumMap<>(SizeMetric.class);
	private final Map<MiscCustomMetric, AtomicInteger> appMiscGauges = new EnumMap<>(MiscCustomMetric.class);

	/**
	 * The Enum PsCustomMetric.
	 */
	public enum SizeMetric {

		/** The ps adeli reference size. */
		REFERENCE_ADELI_SIZE,

		/** The ps finess reference size. */
		REFERENCE_FINESS_SIZE,

		/** The ps siret reference size. */
		REFERENCE_SIRET_SIZE,

		/** The ps rpps reference size. */
		REFERENCE_RPPS_SIZE,

		/** The ps adeli delete size. */
		DELETE_ADELI_SIZE,

		/** The ps finess delete size. */
		DELETE_FINESS_SIZE,

		/** The ps siret delete size. */
		DELETE_SIRET_SIZE,

		/** The ps rpps delete size. */
		DELETE_RPPS_SIZE,

		/** The ps adeli create size. */
		CREATE_ADELI_SIZE,

		/** The ps finess create size. */
		CREATE_FINESS_SIZE,

		/** The ps siret create size. */
		CREATE_SIRET_SIZE,

		/** The ps rpps create size. */
		CREATE_RPPS_SIZE,

		/** The ps adeli update size. */
		UPDATE_ADELI_SIZE,

		/** The ps finess update size. */
		UPDATE_FINESS_SIZE,

		/** The ps siret update size. */
		UPDATE_SIRET_SIZE,

		/** The ps rpps update size. */
		UPDATE_RPPS_SIZE;

		/**
		 * Stream ps metrics.
		 *
		 * @return the stream
		 */
		public static Stream<SizeMetric> stream() {
			return Stream.of(SizeMetric.values());
		}

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
		this.meterRegistry = meterRegistry;
		appMiscGauges.put(MiscCustomMetric.STAGE, meterRegistry.gauge("pscload.stage", new AtomicInteger(0)));

		// Initialization of metrics

		// PS size metrics :
		// initialize metric for each type and operation for a PS
		Arrays.stream(ID_TYPE.values()).forEach(id_type -> Arrays.stream(OPERATION.values()).forEach(operation -> {
			String metricKey = String.join("_", operation.name(), id_type.name(), "SIZE");
			appSizeGauges.put(SizeMetric.valueOf(metricKey),
					meterRegistry.gauge(PS_METRIC_NAME,
							Tags.of(ID_TYPE_TAG, id_type.toString(), OPERATION_TAG, operation.name().toLowerCase()),
							// set to -1 to discriminate no changes on this operation & entity (-> 0) and
							// not the correct stage (-> -1)
							// a -1 value means that we're not at a stage between diff & upload
							// a 0 value means that there are no changes (delete, update, etc) for this
							// particular entity after diff
							new AtomicInteger(-1)));
		}));

		Counter.builder(SER_FILE_TAG).tags(TIMESTAMP_TAG, "").register(meterRegistry);
	}

	/**
	 * Sets the ps metric size.
	 *
	 * @param metric the metric
	 * @param value the value
	 */
	public void setPsMetricSize(SizeMetric metric, int value) {
		appSizeGauges.get(metric).set(value);
	}

	/**
	 * Reset size metrics.
	 */
	public void resetSizeMetrics() {
		// reset all PsSizeMetrics
		Arrays.stream(CustomMetrics.ID_TYPE.values()).forEach(id_type -> {
			Arrays.stream(CustomMetrics.OPERATION.values()).forEach(operation -> {
				String metricKey = String.join("_", operation.name(), id_type.name(), "SIZE");
				appSizeGauges.get(SizeMetric.valueOf(metricKey)).set(-1);
			});
		});
	}

	public void setStageMetric(Stage state) {
		appMiscGauges.get(MiscCustomMetric.STAGE).set(state.value);
	}

	public void setStageMetric(int value) {
		appMiscGauges.get(MiscCustomMetric.STAGE).set(value);
	}

	public Map<MiscCustomMetric, AtomicInteger> getAppMiscGauges() {
		return appMiscGauges;
	}

	public int getStageMetricValue() {
		return appMiscGauges.get(MiscCustomMetric.STAGE).get();
	}

	public Map<SizeMetric, AtomicInteger> getAppSizeGauges() {
		return appSizeGauges;
	}
}
