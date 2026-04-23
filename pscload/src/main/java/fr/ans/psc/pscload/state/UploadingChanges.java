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
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.metrics.CustomMetrics.ID_TYPE;
import fr.ans.psc.pscload.metrics.CustomMetrics.SizeMetric;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.service.MessageProducer;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import fr.ans.psc.pscload.state.exception.LockedMapException;
import fr.ans.psc.pscload.state.exception.UploadException;
import fr.ans.psc.pscload.visitor.MapsUploaderVisitorImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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

		MapsUploaderVisitorImpl visitor = new MapsUploaderVisitorImpl(excludedProfessions, apiBaseUrl, messageProducer);

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
            Map<SizeMetric, Integer> effective = visitor.getEffectiveCounts();
            Map<String, Integer> failures = visitor.getFailureCounts();
            process.setEffectiveCounts(effective);
            process.setEffectiveFailures(failures);
            logSummary(effective, failures);
            log.info("API operations done.");
        } catch (LockedMapException e) {
            log.error("Shutdown was initiated during Uploading Changes stage.");
            throw new UploadException("Shutdown was initiated during Uploading Changes stage.");
        }

    }

    private void logSummary(Map<SizeMetric, Integer> effective, Map<String, Integer> failures) {
        log.info("===== PSCLOAD EFFECTIVE OPERATIONS SUMMARY =====");
        log.info("CREATE (HTTP 201) : {}", formatLine("CREATE", effective));
        log.info("UPDATE (HTTP 200) : {}", formatLine("UPDATE", effective));
        log.info("DELETE (HTTP 204) : {}", formatLine("DELETE", effective));
        if (failures.isEmpty()) {
            log.info("FAILURES          : none");
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            failures.forEach((k, v) -> joiner.add(k + "=" + v));
            log.info("FAILURES          : {}", joiner.toString());
        }
        log.info("================================================");
    }

    private String formatLine(String operation, Map<SizeMetric, Integer> effective) {
        StringJoiner joiner = new StringJoiner(" ");
        for (ID_TYPE idType : ID_TYPE.values()) {
            SizeMetric key = SizeMetric.valueOf(operation + "_" + idType.name() + "_SIZE");
            joiner.add(idType.name() + "=" + effective.getOrDefault(key, 0));
        }
        return joiner.toString();
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
