package fr.ans.psc.pscload.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;

import com.google.common.collect.MapDifference;

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

	private MapDifference<String, Ps> psMap;

	private MapDifference<String, Structure> structureMap;

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

	public MapDifference<String, Ps> getPsMap() {
		return psMap;
	}

	public void setPsMap(MapDifference<String, Ps> psMap) {
		this.psMap = psMap;
	}

	public MapDifference<String, Structure> getStructureMap() {
		return structureMap;
	}

	public void setStructureMap(MapDifference<String, Structure> structureMap) {
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

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		timestamp = in.readLong();
		downloadedFilename = (String) in.readObject();
		extractedFilename = (String) in.readObject();
		state = (ProcessState) in.readObject();
		psMap = (MapDifference<String, Ps>) in.readObject();
		structureMap = (MapDifference<String, Structure>) in.readObject();
	}

}
