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
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.visitor.OperationType;

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

		publishUploadMetrics();
		publishPsMetrics();
		publishStructureMetrics();
	}

	@Override
	public boolean isAlreadyComputed() {
		return true;
	}

	private void publishPsMetrics() {
		
//		PsCustomMetric.stream().filter(m -> m.name().contains("ADELI")).forEach(m -> {
//			customMetrics.getPsSizeGauges().get(m);});
		OperationMap<String,RassEntity> psToDelete = process.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.PS_DELETE))
		.findFirst().get();
		OperationMap<String,RassEntity> psToUpdate = process.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.PS_UPDATE))
		.findFirst().get();
		OperationMap<String,RassEntity> psToCreate = process.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.PS_CREATE))
		.findFirst().get();

		
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_ADELI_DELETE_SIZE)
				.set(Math.toIntExact(psToDelete.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.ADELI.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_ADELI_CREATE_SIZE)
				.set(Math.toIntExact(psToCreate.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.ADELI.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_ADELI_UPDATE_SIZE)
				.set(Math.toIntExact(psToUpdate.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.ADELI.value.equals(ps.getIdType())).count()));

		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_FINESS_DELETE_SIZE)
				.set(Math.toIntExact(psToDelete.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.FINESS.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_FINESS_CREATE_SIZE)
				.set(Math.toIntExact(psToCreate.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.FINESS.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_FINESS_UPDATE_SIZE)
				.set(Math.toIntExact(psToUpdate.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.FINESS.value.equals(ps.getIdType())).count()));

		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_SIRET_DELETE_SIZE)
				.set(Math.toIntExact(psToDelete.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.SIRET.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_SIRET_CREATE_SIZE)
				.set(Math.toIntExact(psToCreate.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.SIRET.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_SIRET_UPDATE_SIZE)
				.set(Math.toIntExact(psToUpdate.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.SIRET.value.equals(ps.getIdType())).count()));

		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_RPPS_DELETE_SIZE)
				.set(Math.toIntExact(psToDelete.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.RPPS.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_RPPS_CREATE_SIZE)
				.set(Math.toIntExact(psToCreate.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.RPPS.value.equals(ps.getIdType())).count()));
		customMetrics.getPsSizeGauges().get(PsCustomMetric.PS_RPPS_UPDATE_SIZE)
				.set(Math.toIntExact(psToUpdate.values().stream()
						.filter(ps -> CustomMetrics.ID_TYPE.RPPS.value.equals(ps.getIdType())).count()));

	}

	private void publishStructureMetrics() {
		customMetrics.getAppStructureSizeGauges().get(StructureCustomMetric.STRUCTURE_CREATE_SIZE)
				.set(process.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.STRUCTURE_CREATE))
						.findFirst().get().size());
		customMetrics.getAppStructureSizeGauges().get(StructureCustomMetric.STRUCTURE_UPDATE_SIZE)
				.set(process.getMaps().stream().filter(map -> map.getOperation().equals(OperationType.STRUCTURE_UPDATE))
						.findFirst().get().size());
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

	@Override
	public void write(Kryo kryo, Output output) {
	}

	@Override
	public void read(Kryo kryo, Input input) {
	}
}
