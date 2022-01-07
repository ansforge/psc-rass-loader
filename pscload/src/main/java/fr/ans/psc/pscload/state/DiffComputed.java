/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.metrics.CustomMetrics.PsCustomMetric;
import fr.ans.psc.pscload.metrics.CustomMetrics.StructureCustomMetric;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.visitor.MapsMetricsSetterVisitorImpl;
import fr.ans.psc.pscload.visitor.MapsVisitor;

/**
 * The Class DiffComputed.
 */
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

	@Override
	public void nextStep() {

		publishReferenceMetrics();

		MapsVisitor visitor = new MapsMetricsSetterVisitorImpl(customMetrics);
		for (OperationMap<String, RassEntity> map : process.getMaps()) {
			map.accept(visitor);
		}
	}

	@Override
	public boolean isAlreadyComputed() {
		return true;
	}

	private void publishReferenceMetrics() {

		customMetrics.setPsMetricSize(PsCustomMetric.PS_ADELI_REFERENCE_SIZE,
				process.getUploadMetrics().getPsAdeliReferenceSize());
		customMetrics.setPsMetricSize(PsCustomMetric.PS_FINESS_REFERENCE_SIZE,
				process.getUploadMetrics().getPsAdeliReferenceSize());
		customMetrics.setPsMetricSize(PsCustomMetric.PS_SIRET_REFERENCE_SIZE,
				process.getUploadMetrics().getPsAdeliReferenceSize());
		customMetrics.setPsMetricSize(PsCustomMetric.PS_RPPS_REFERENCE_SIZE,
				process.getUploadMetrics().getPsAdeliReferenceSize());
		customMetrics.setStructureMetricSize(StructureCustomMetric.STRUCTURE_REFERENCE_SIZE,
				process.getUploadMetrics().getPsAdeliReferenceSize());
	}

	@Override
	public void write(Kryo kryo, Output output) {
	}

	@Override
	public void read(Kryo kryo, Input input) {
	}
}
