package fr.ans.psc.pscload.state;

import java.io.Serializable;

import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

public abstract class ProcessState implements Serializable {
	
	private static final long serialVersionUID = -7973864026513440841L;
	protected LoadProcess process;
	
	abstract public void runTask() throws LoadProcessException;
	
	public LoadProcess getProcess() {
		return process;
	}

	public void setProcess(LoadProcess process) {
		this.process = process;
	}

}
