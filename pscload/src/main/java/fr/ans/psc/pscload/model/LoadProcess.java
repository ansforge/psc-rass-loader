/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscload.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.model.operations.OperationMapFactory;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import fr.ans.psc.pscload.model.operations.OperationType;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class LoadProcess.
 */
@Getter
@Setter
public class LoadProcess implements KryoSerializable {

    private String downloadedFilename;

    private String extractedFilename;

    private String tmpMapsPath;

    private List<OperationMap<String, RassEntity>> maps = new ArrayList<>();

    private long timestamp;

    private ProcessState state;

    private String id;

    /**
     * Instantiates a new load process.
     */
    public LoadProcess() {
        super();
        init();
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
        init();
    }

    private void init() {
        timestamp = Calendar.getInstance().getTimeInMillis();
        Arrays.asList(OperationType.values()).forEach(operation -> {
            maps.add(OperationMapFactory.getOperationMap(operation));
        });
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
        int count = 0;
        for (@SuppressWarnings("rawtypes") OperationMap map : maps) {
            count += map.size();
        }
        return count > 0;

    }

    public ProcessInfo getProcessInfos(boolean withDetails) {
        ProcessInfo processInfo = new ProcessInfo();
        processInfo.setProcessId(id);
        DateFormat df = new SimpleDateFormat();
        processInfo.setCreatedOn(df.format(new Date(timestamp)));
        processInfo.setState(state.getClass().getSimpleName());
        processInfo.setDownloadedFileName(downloadedFilename);
        processInfo.setExtractFileName(extractedFilename);
        processInfo.setLockedSerializedFileName(tmpMapsPath);
        if (state.isAlreadyComputed()) {
            processInfo.setPsToCreateCount(maps.stream().filter(map -> map.getOperation().equals(OperationType.CREATE))
                    .findFirst().get().size());
            processInfo.setPsToUpdateCount(maps.stream().filter(map -> map.getOperation().equals(OperationType.UPDATE))
                    .findFirst().get().size());
            processInfo.setPsToDeleteCount(maps.stream().filter(map -> map.getOperation().equals(OperationType.DELETE))
                    .findFirst().get().size());

            if (withDetails) {
                processInfo.setPsToCreateIds(new ArrayList<>(maps.stream()
                        .filter(map -> map.getOperation().equals(OperationType.CREATE)).findFirst().get().keySet()));
                processInfo.setPsToUpdateIds(new ArrayList<>(maps.stream()
                        .filter(map -> map.getOperation().equals(OperationType.UPDATE)).findFirst().get().keySet()));
                processInfo.setPsToDeleteIds(new ArrayList<>(maps.stream()
                        .filter(map -> map.getOperation().equals(OperationType.DELETE)).findFirst().get().keySet()));
            }
        }

        return processInfo;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(id);
        output.writeLong(timestamp);
        output.writeString(downloadedFilename);
        output.writeString(extractedFilename);
        output.writeString(tmpMapsPath);
        // We need to write the class also because state is an abstract class (hope
        // never null)
        kryo.writeClassAndObject(output, state);
        kryo.writeObjectOrNull(output, maps, ArrayList.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        id = input.readString();
        timestamp = input.readLong();
        downloadedFilename = input.readString();
        extractedFilename = input.readString();
        tmpMapsPath = input.readString();
        state = (ProcessState) kryo.readClassAndObject(input);
        maps = (List<OperationMap<String, RassEntity>>) kryo.readObjectOrNull(input, ArrayList.class);
    }

}
