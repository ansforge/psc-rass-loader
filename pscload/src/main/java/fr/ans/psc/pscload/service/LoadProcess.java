package fr.ans.psc.pscload.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;
import java.util.Map;

import fr.ans.psc.model.Ps;
import fr.ans.psc.model.Structure;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

public class LoadProcess implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5600982089854286505L;

	private String downloadedFilename;

	private String extractedFilename;

	private Map<String, Ps> psMap;

	private Map<String, Structure> structureMap;

	private long timestamp;

	private ProcessState state;

	public LoadProcess() {
		super();
	}

	public LoadProcess(ProcessState state) {
		super();
		this.state = state;
		this.state.setProcess(this);
		timestamp = Calendar.getInstance().getTimeInMillis();
	}

	public void runtask() throws LoadProcessException {
		state.runTask();
	}

	public ProcessState getState() {
		return state;
	}

	public void setState(ProcessState state) {
		this.state = state;
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

	public Map<String, Ps> getPsMap() {
		return psMap;
	}

	public void setPsMap(Map<String, Ps> psMap) {
		this.psMap = psMap;
	}

	public Map<String, Structure> getStructureMap() {
		return structureMap;
	}

	public void setStructureMap(Map<String, Structure> structureMap) {
		this.structureMap = structureMap;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(timestamp);
		out.writeObject(downloadedFilename);
		out.writeObject(extractedFilename);
		out.writeObject(state);
		out.writeObject(psMap);
		out.writeObject(structureMap);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		timestamp = in.readLong();
		downloadedFilename = (String) in.readObject();
		extractedFilename = (String) in.readObject();
		state = (ProcessState) in.readObject();
		psMap = (Map<String, Ps>) in.readObject();
		structureMap = (Map<String, Structure>) in.readObject();
	}

}
