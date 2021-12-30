package fr.ans.psc.pscload.state;

import fr.ans.psc.pscload.state.exception.LoadProcessException;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SerializationInterrupted extends ProcessState {
    @Override
    public void nextStep() throws LoadProcessException {

    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
