/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.metrics.UploadMetrics;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class LoadProcess.
 */
@Getter
@Setter
public class LoadProcess implements KryoSerializable {

    /**
     *
     */
    private static final long serialVersionUID = 5600982089854286505L;

    private String downloadedFilename;

    private String extractedFilename;

    private Map<String, Professionnel> psToCreate;

    private String tmpMapsPath;

    private Map<String, Professionnel> psToUpdate;

    private Map<String, Professionnel> psToDelete;

    private Map<String, Structure> structureToCreate;

    private Map<String, Structure> structureToUpdate;

    private long timestamp;

    private ProcessState state;

    private String id;

    private UploadMetrics uploadMetrics = new UploadMetrics();

    /**
     * Instantiates a new load process.
     */
    public LoadProcess() {
        super();
    }

    /**
     * Instantiates a new load process.
     *
     * @param state the state
     */
    public LoadProcess(ProcessState state) {
        super();
        this.state = state;
        this.state.setProcess(this);
        timestamp = Calendar.getInstance().getTimeInMillis();
        psToUpdate = new ConcurrentHashMap<String, Professionnel>();
        structureToUpdate = new ConcurrentHashMap<String, Structure>();
    }

    /**
     * Instantiates a new load process.
     *
     * @param state the state
     * @param id    the id
     */
    public LoadProcess(ProcessState state, String id) {
        this(state);
        this.id = id;
    }

    /**
     * Runtask.
     *
     * @throws LoadProcessException the load process exception
     */
    public void nextStep() throws LoadProcessException {
        state.nextStep();
    }

    public void setState(ProcessState state) {
        this.state = state;
        state.setProcess(this);
    }


    public boolean isRemainingPsOrStructuresInMaps() {
        return psToCreate.size() + psToDelete.size() + psToUpdate.size()
                + structureToCreate.size() + structureToUpdate.size() > 0;
    }

	@Override
	public void write(Kryo kryo, Output output) {
        output.writeString(id);
        output.writeLong(timestamp);
        output.writeString(downloadedFilename);
        output.writeString(extractedFilename);
        // We need to write the class also because state is an abstract class (hope never null)
        kryo.writeClassAndObject(output,state);
        kryo.writeObjectOrNull(output,psToCreate, HashMap.class);
        kryo.writeObjectOrNull(output,psToUpdate, HashMap.class);
        kryo.writeObjectOrNull(output,psToDelete, HashMap.class);
        kryo.writeObjectOrNull(output,structureToCreate, HashMap.class);
        kryo.writeObjectOrNull(output,structureToUpdate, HashMap.class);
        kryo.writeObjectOrNull(output,uploadMetrics, UploadMetrics.class);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
        id = input.readString();
        timestamp = input.readLong();
        downloadedFilename = input.readString();
        extractedFilename = input.readString();
        state = (ProcessState) kryo.readClassAndObject(input);
        psToCreate = (Map<String, Professionnel>) kryo.readObjectOrNull(input, HashMap.class);
        psToUpdate = (Map<String, Professionnel>) kryo.readObjectOrNull(input, HashMap.class);
        psToDelete = (Map<String, Professionnel>) kryo.readObjectOrNull(input, HashMap.class);
        structureToCreate = (Map<String, Structure>) kryo.readObjectOrNull(input, HashMap.class);
        structureToUpdate = (Map<String, Structure>) kryo.readObjectOrNull(input, HashMap.class);
        uploadMetrics = (UploadMetrics) kryo.readObjectOrNull(input, UploadMetrics.class);
		
	}

}
