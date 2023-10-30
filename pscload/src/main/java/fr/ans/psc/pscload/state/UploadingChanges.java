/**
 * Copyright (C) 2022-2023 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.service.MessageProducer;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import fr.ans.psc.pscload.state.exception.LockedMapException;
import fr.ans.psc.pscload.state.exception.UploadException;
import fr.ans.psc.pscload.visitor.MapsUploaderVisitorImpl;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * The Class UploadingChanges.
 */
@Slf4j
public class UploadingChanges extends ProcessState {


    private String[] excludedProfessions;

    private String apiBaseUrl;

    private List<String> excludedOperations;

    private MessageProducer messageProducer;

    /**
     * Instantiates a new Uploading Changes.
     *
     * @param excludedProfessions the excluded professions
     * @param apiBaseUrl          the api base url
     */
    public UploadingChanges(String[] excludedProfessions, String apiBaseUrl) {
        this(excludedProfessions, apiBaseUrl, null, null);
    }

    public UploadingChanges(String[] excludedProfessions, String apiBaseUrl,
                            List<String> excludedOperations, MessageProducer messageProducer) {
        this.excludedProfessions = excludedProfessions;
        this.apiBaseUrl = apiBaseUrl;
        this.excludedOperations = excludedOperations;
        this.messageProducer = messageProducer;
    }

    /**
     * Instantiates a new uploading changes.
     */
    public UploadingChanges() {
		super();
	}

	@Override
    public void nextStep() throws LoadProcessException {
        log.info("calling API...");
    	
		MapsVisitor visitor = new MapsUploaderVisitorImpl(excludedProfessions, apiBaseUrl, messageProducer);

        List<OperationMap<String, RassEntity>> processMaps = process.getMaps();
        if (excludedOperations != null) {
            String ops = String.join(",", excludedOperations);
            log.info("these operations won't be processed : {}", ops);
            processMaps.removeIf(map -> excludedOperations.contains(map.getOperation().name()));
        }

		try {
            for (OperationMap<String, RassEntity> map : processMaps) {
                map.accept(visitor);
            }
            log.info("API operations done.");
        } catch (LockedMapException e) {
            log.error("Shutdown was initiated during Uploading Changes stage.");
            throw new UploadException("Shutdown was initiated during Uploading Changes stage.");
        }

    }

    @Override
	public void write(Kryo kryo, Output output) {
    	kryo.writeClassAndObject(output, excludedProfessions);
    	kryo.writeClassAndObject(output, excludedOperations);
    	output.writeString(apiBaseUrl);
    }

    @Override
    @SuppressWarnings("unchecked")
	public void read(Kryo kryo, Input input) {
        excludedProfessions = (String[]) kryo.readClassAndObject(input);
        excludedOperations = (List<String>) kryo.readClassAndObject(input);
        apiBaseUrl = input.readString();
    }

    @Override
    public boolean isAlreadyComputed() {
        return true;
    }

    @Override
    public boolean isExpirable() { return false;}
}
