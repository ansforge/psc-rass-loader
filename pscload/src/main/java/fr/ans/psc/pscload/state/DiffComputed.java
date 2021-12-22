/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.metrics.CustomMetrics.PsCustomMetric;
import fr.ans.psc.pscload.metrics.CustomMetrics.StructureCustomMetric;

/**
 * The Class DiffComputed.
 */
public class DiffComputed extends ProcessState {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2486351862090505174L;

	private CustomMetrics customMetrics;

	public DiffComputed() {
		super();
	}

	public DiffComputed(CustomMetrics customMetrics) {
		super();
		this.customMetrics = customMetrics;
	}

	@Override
	public void nextStep() {

		publishUploadMetrics();
		publishPsMetrics();
		publishStructureMetrics();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub

	}

	private void publishPsMetrics() {
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_ADELI_DELETE_SIZE)
				.set(Math.toIntExact(process.getPsToDelete().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.ADELI.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_ADELI_CREATE_SIZE)
				.set(Math.toIntExact(process.getPsToCreate().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.ADELI.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_ADELI_UPDATE_SIZE)
				.set(Math.toIntExact(process.getPsToUpdate().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.ADELI.value.equals(ps.getIdType())).count()));

		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_FINESS_DELETE_SIZE)
				.set(Math.toIntExact(process.getPsToDelete().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.FINESS.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_FINESS_CREATE_SIZE)
				.set(Math.toIntExact(process.getPsToCreate().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.FINESS.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_FINESS_UPDATE_SIZE)
				.set(Math.toIntExact(process.getPsToUpdate().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.FINESS.value.equals(ps.getIdType())).count()));

		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_SIRET_DELETE_SIZE)
				.set(Math.toIntExact(process.getPsToDelete().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.SIRET.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_SIRET_CREATE_SIZE)
				.set(Math.toIntExact(process.getPsToCreate().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.SIRET.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_SIRET_UPDATE_SIZE)
				.set(Math.toIntExact(process.getPsToUpdate().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.SIRET.value.equals(ps.getIdType())).count()));

		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_RPPS_DELETE_SIZE)
				.set(Math.toIntExact(process.getPsToDelete().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.RPPS.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_RPPS_CREATE_SIZE)
				.set(Math.toIntExact(process.getPsToCreate().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.RPPS.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_RPPS_UPDATE_SIZE)
				.set(Math.toIntExact(process.getPsToUpdate().values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.RPPS.value.equals(ps.getIdType())).count()));

	}

	private void publishStructureMetrics() {
		customMetrics.getAppStructureSizeGauges().get(StructureCustomMetric.STRUCTURE_DELETE_SIZE)
				.set(process.getStructureToDelete().size());
		customMetrics.getAppStructureSizeGauges().get(StructureCustomMetric.STRUCTURE_CREATE_SIZE)
				.set(process.getStructureToCreate().size());
		customMetrics.getAppStructureSizeGauges().get(StructureCustomMetric.STRUCTURE_UPDATE_SIZE)
				.set(process.getStructureToUpdate().size());
	}

	private void publishUploadMetrics() {

		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_ADELI_UPLOAD_SIZE)
				.set(process.getUploadMetrics().getPsAdeliUploadSize());
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_FINESS_UPLOAD_SIZE)
				.set(process.getUploadMetrics().getPsFinessUploadSize());
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_SIRET_UPLOAD_SIZE)
				.set(process.getUploadMetrics().getPsSiretUploadSize());
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_RPPS_UPLOAD_SIZE)
				.set(process.getUploadMetrics().getPsRppsUploadSize());
		customMetrics.getAppStructureSizeGauges().get(StructureCustomMetric.STRUCTURE_UPLOAD_SIZE)
				.set(process.getUploadMetrics().getStructureUploadSize());

	}
}
