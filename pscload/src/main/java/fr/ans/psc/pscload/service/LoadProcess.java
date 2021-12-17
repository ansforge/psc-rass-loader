/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;
import java.util.Map;

import com.google.common.collect.MapDifference.ValueDifference;

import fr.ans.psc.pscload.metrics.UploadMetrics;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

/**
 * The Class LoadProcess.
 */
public class LoadProcess implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5600982089854286505L;

	private String downloadedFilename;

	private String extractedFilename;

	private Map<String, Professionnel> psToCreate;
	
	private Map<String, ValueDifference<Professionnel>> psToUpdate;
	
	private Map<String, Professionnel> psToDelete;

	private Map<String, Structure> structureToCreate;
	
	private Map<String, ValueDifference<Structure>> structureToUpdate;
	
	private Map<String, Structure> structureToDelete;

	private long timestamp;

	private ProcessState state;
	
	private String id;
	
	private UploadMetrics uploadMetrics;

	/**
	 * Instantiates a new load process.
	 */
	public LoadProcess() {
		super();
	}

	/**
	 * Instantiates a new load process.
	 *
	 * @param state the state
	 */
	public LoadProcess(ProcessState state) {
		super();
		this.state = state;
		this.state.setProcess(this);
		timestamp = Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * Instantiates a new load process.
	 *
	 * @param state the state
	 * @param id the id
	 */
	public LoadProcess(ProcessState state, String id) {
		this(state);
		this.id = id;
	}

	/**
	 * Runtask.
	 *
	 * @throws LoadProcessException the load process exception
	 */
	public void runtask() throws LoadProcessException {
		state.runTask();
	}

	public ProcessState getState() {
		return state;
	}

	public void setState(ProcessState state) {
		this.state = state;
		state.setProcess(this);
	}
	
	public Map<String, Professionnel> getPsToCreate() {
		return psToCreate;
	}

	public void setPsToCreate(Map<String, Professionnel> psToCreate) {
		this.psToCreate = psToCreate;
	}

	public Map<String, ValueDifference<Professionnel>> getPsToUpdate() {
		return psToUpdate;
	}

	public void setPsToUpdate(Map<String, ValueDifference<Professionnel>> psToUpdate) {
		this.psToUpdate = psToUpdate;
	}

	public Map<String, Professionnel> getPsToDelete() {
		return psToDelete;
	}

	public void setPsToDelete(Map<String, Professionnel> psToDelete) {
		this.psToDelete = psToDelete;
	}

	public Map<String, Structure> getStructureToCreate() {
		return structureToCreate;
	}

	public void setStructureToCreate(Map<String, Structure> structureToCreate) {
		this.structureToCreate = structureToCreate;
	}

	public Map<String, ValueDifference<Structure>> getStructureToUpdate() {
		return structureToUpdate;
	}

	public void setStructureToUpdate(Map<String, ValueDifference<Structure>> structureToUpdate) {
		this.structureToUpdate = structureToUpdate;
	}

	public Map<String, Structure> getStructureToDelete() {
		return structureToDelete;
	}

	public void setStructureToDelete(Map<String, Structure> structureToDelete) {
		this.structureToDelete = structureToDelete;
	}

	public String getDownloadedFilename() {
		return downloadedFilename;
	}

	public void setDownloadedFilename(String downloadedFilename) {
		this.downloadedFilename = downloadedFilename;
	}

	public String getExtractedFilename() {
		return extractedFilename;
	}

	public void setExtractedFilename(String extractedFilename) {
		this.extractedFilename = extractedFilename;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public UploadMetrics getUploadMetrics() {
		return uploadMetrics;
	}

	public void setUploadMetrics(UploadMetrics uploadMetrics) {
		this.uploadMetrics = uploadMetrics;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeLong(timestamp);
		out.writeObject(downloadedFilename);
		out.writeObject(extractedFilename);
		out.writeObject(state);
		out.writeObject(psToCreate);
		out.writeObject(psToUpdate);
		out.writeObject(psToDelete);
		out.writeObject(structureToCreate);
		out.writeObject(structureToUpdate);
		out.writeObject(structureToDelete);
		out.writeObject(uploadMetrics);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = (String) in.readObject();
		timestamp = in.readLong();
		downloadedFilename = (String) in.readObject();
		extractedFilename = (String) in.readObject();
		state = (ProcessState) in.readObject();
		psToCreate = (Map<String, Professionnel>) in.readObject();
		psToUpdate = (Map<String, ValueDifference<Professionnel>>) in.readObject();
		psToDelete = (Map<String, Professionnel>) in.readObject();
		structureToCreate = (Map<String, Structure>) in.readObject();
		structureToUpdate = (Map<String, ValueDifference<Structure>>) in.readObject();
		structureToDelete = (Map<String, Structure>) in.readObject();
		uploadMetrics = (UploadMetrics) in.readObject();
	}

}
