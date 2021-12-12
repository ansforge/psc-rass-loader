package fr.ans.psc.pscload.state;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ChangesApplied extends ProcessState {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2486351862090505174L;

	@Override
	public void runTask() {
		// TODO call pscextract and deregister process now because we can't know the status of pscextract.

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

}
