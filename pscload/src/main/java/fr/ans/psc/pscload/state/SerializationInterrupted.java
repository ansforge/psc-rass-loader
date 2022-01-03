package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.state.exception.LoadProcessException;

public class SerializationInterrupted extends ProcessState {

	public SerializationInterrupted() {
		super();
	}

    @Override
    public void nextStep() throws LoadProcessException {
    }

	@Override
	public void write(Kryo kryo, Output output) {
	}

	@Override
	public void read(Kryo kryo, Input input) {
	}

	@Override
	public boolean isAlreadyComputed() {
		return true;
	}
}
