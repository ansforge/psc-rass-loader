package fr.ans.psc.pscload.service;


import com.google.common.collect.MapDifference;

import fr.ans.psc.model.Ps;
import fr.ans.psc.model.Structure;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

public class LoadProcess {
	
	private String donwloadedFilename;
	
	private String extractedFilename;
	
	private MapDifference<String,Ps> currentMap;
	
	private MapDifference<String, Structure> lastMap;
	
	private ProcessState state;

	
	public LoadProcess(ProcessState state) {
		super();
		this.state = state;
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


	public String getDonwloadedFilename() {
		return donwloadedFilename;
	}


	public void setDonwloadedFilename(String donwloadedFilename) {
		this.donwloadedFilename = donwloadedFilename;
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


	

}
