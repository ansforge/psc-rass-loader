/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    private ConcurrentMap<String, Professionnel> psToCreate;

    private String tmpMapsPath;

    private ConcurrentMap<String, Professionnel> psToUpdate;

    private ConcurrentMap<String, Professionnel> psToDelete;

    private ConcurrentMap<String, Structure> structureToCreate;

    private ConcurrentMap<String, Structure> structureToUpdate;

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
        kryo.writeObjectOrNull(output,psToCreate, ConcurrentHashMap.class);
        kryo.writeObjectOrNull(output,psToUpdate, ConcurrentHashMap.class);
        kryo.writeObjectOrNull(output,psToDelete, ConcurrentHashMap.class);
        kryo.writeObjectOrNull(output,structureToCreate, ConcurrentHashMap.class);
        kryo.writeObjectOrNull(output,structureToUpdate, ConcurrentHashMap.class);
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
        psToCreate = (ConcurrentMap<String, Professionnel>) kryo.readObjectOrNull(input, ConcurrentHashMap.class);
        psToUpdate = (ConcurrentMap<String, Professionnel>) kryo.readObjectOrNull(input, ConcurrentHashMap.class);
        psToDelete = (ConcurrentMap<String, Professionnel>) kryo.readObjectOrNull(input, ConcurrentHashMap.class);
        structureToCreate = (ConcurrentMap<String, Structure>) kryo.readObjectOrNull(input, ConcurrentHashMap.class);
        structureToUpdate = (ConcurrentMap<String, Structure>) kryo.readObjectOrNull(input, ConcurrentHashMap.class);
        uploadMetrics = (UploadMetrics) kryo.readObjectOrNull(input, UploadMetrics.class);
		
	}

}
