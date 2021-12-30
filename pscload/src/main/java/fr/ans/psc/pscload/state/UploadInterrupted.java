package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class UploadInterrupted extends ProcessState {

    @Override
    public void nextStep() throws LoadProcessException {

    }

    @Override
    public void write(Kryo kryo, Output output) {

    }

    @Override
    public void read(Kryo kryo, Input input) {

    }
}
