package fr.ans.psc.pscload.service;


import java.io.Serializable;
import java.util.Calendar;

import com.google.common.collect.MapDifference;

import fr.ans.psc.model.Ps;
import fr.ans.psc.model.Structure;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

public class LoadProcess implements Serializable {
	
	private static final long serialVersionUID = -3353029432198849016L;

	private String downloadedFilename;
	
	private String extractedFilename;
	
	private transient MapDifference<String,Ps> currentMap;
	
	private transient MapDifference<String, Structure> lastMap;
	
	private long timestamp;
	
	private ProcessState state;

	
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


	public MapDifference<String, Ps> getCurrentMap() {
		return currentMap;
	}


	public void setCurrentMap(MapDifference<String, Ps> currentMap) {
		this.currentMap = currentMap;
	}


	public MapDifference<String, Structure> getLastMap() {
		return lastMap;
	}


	public void setLastMap(MapDifference<String, Structure> lastMap) {
		this.lastMap = lastMap;
	}

	public long getTimestamp() {
		return timestamp;
	}
	

}
