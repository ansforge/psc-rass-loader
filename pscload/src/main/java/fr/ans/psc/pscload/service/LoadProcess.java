/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.ans.psc.pscload.metrics.UploadMetrics;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.SerializableValueDifference;
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
public class LoadProcess implements Externalizable {

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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeLong(timestamp);
        out.writeObject(downloadedFilename);
        out.writeObject(extractedFilename);
        out.writeObject(state);
        out.writeObject(psToCreate);
        out.writeObject(psToUpdate);
        out.writeObject(psToDelete);
        out.writeObject(structureToCreate);
        out.writeObject(structureToUpdate);
        out.writeObject(uploadMetrics);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (String) in.readObject();
        timestamp = in.readLong();
        downloadedFilename = (String) in.readObject();
        extractedFilename = (String) in.readObject();
        state = (ProcessState) in.readObject();
        psToCreate = (Map<String, Professionnel>) in.readObject();
        psToUpdate = (Map<String, Professionnel>) in.readObject();
        psToDelete = (Map<String, Professionnel>) in.readObject();
        structureToCreate = (Map<String, Structure>) in.readObject();
        structureToUpdate = (Map<String, Structure>) in.readObject();
        uploadMetrics = (UploadMetrics) in.readObject();
    }

    public boolean isRemainingPsOrStructuresInMaps() {
        return psToCreate.size() + psToDelete.size() + psToUpdate.size()
                + structureToCreate.size() + structureToUpdate.size() > 0;
    }

}
