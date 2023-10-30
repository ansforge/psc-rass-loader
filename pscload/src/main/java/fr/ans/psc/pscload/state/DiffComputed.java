/**
 * Copyright (C) 2022-2023 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscload.state;

import java.util.Arrays;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.visitor.MapsMetricsSetterVisitorImpl;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class DiffComputed.
 */
@Slf4j
public class DiffComputed extends ProcessState {

	private CustomMetrics customMetrics;

	/**
	 * Instantiates a new diff computed.
	 *
	 * @param customMetrics the custom metrics
	 */
	public DiffComputed(CustomMetrics customMetrics) {
		super();
		this.customMetrics = customMetrics;
	}

	/**
	 * Instantiates a new diff computed.
	 */
	public DiffComputed() {
		super();
	}

	@Override
	public void nextStep() {
		log.info("publishing metrics...");
		logReferenceMetrics();

		MapsVisitor visitor = new MapsMetricsSetterVisitorImpl(customMetrics);
		for (OperationMap<String, RassEntity> map : process.getMaps()) {
			map.accept(visitor);
		}
	}

	private void logReferenceMetrics() {
		Arrays.stream(CustomMetrics.ID_TYPE.values()).forEach(id_type -> {
			String metricKey = String.join("_", "REFERENCE", id_type.name(), "SIZE");
			CustomMetrics.SizeMetric metric = CustomMetrics.SizeMetric.valueOf(metricKey);

			log.info("{} --- {}", metricKey, customMetrics.getAppSizeGauges().get(metric).get());
		});
	}

	@Override
	public boolean isAlreadyComputed() {
		return true;
	}


	@Override
	public void write(Kryo kryo, Output output) {
	}

	@Override
	public void read(Kryo kryo, Input input) {
	}

	public CustomMetrics getCustomMetrics() {
		return customMetrics;
	}
}
