package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.state.exception.LoadProcessException;

public class UploadInterrupted extends ProcessState {

    @Override
    public void nextStep() throws LoadProcessException {

    }
  
	@Override
	public void write(Kryo kryo, Output output) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void read(Kryo kryo, Input input) {
		// TODO Auto-generated method stub
		
	}

}
