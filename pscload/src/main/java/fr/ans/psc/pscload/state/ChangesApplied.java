/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.exception.ChangesApplicationException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ChangesApplied.
 */
@Slf4j
public class ChangesApplied extends ProcessState {

    private CustomMetrics customMetrics;
    private String extractBaseUrl;

    private MapsHandler newMaps = new MapsHandler();
    private MapsHandler oldMaps = new MapsHandler();

    public ChangesApplied(CustomMetrics customMetrics, String extractBaseUrl) {
        super();
        this.customMetrics = customMetrics;
        this.extractBaseUrl = extractBaseUrl;
    }

    public ChangesApplied() {}


    @Override
    public void nextStep() {
        String lockedFilePath = process.getTmpMapsPath();
        String serFileName = new File(lockedFilePath).getParent() + File.separator + "maps.ser";
        File serFile = new File(serFileName);

        try {
        	newMaps.deserializeMaps(lockedFilePath);
            if (serFile.exists()) {
            	oldMaps.deserializeMaps(serFileName);
            }
        } catch (IOException | ClassNotFoundException e) {
            String msgLogged = e.getClass().equals(IOException.class) ? "Error during deserialization" : "Serialized file not found";
            log.error(msgLogged, e.getLocalizedMessage());
            throw new ChangesApplicationException(e);
        }


        if (process.isRemainingPsOrStructuresInMaps()) {
            StringBuilder message = new StringBuilder();
            handlePsCreateFailed(message, process.getPsToCreate());
            handlePsDeleteFailed(message, process.getPsToDelete());
            handlePsUpdateFailed(message, process.getPsToUpdate());

            handleStructureCreateFailed(message, process.getStructureToCreate());
            handleStructureUpdateFailed(message, process.getStructureToUpdate());

            message.append("Si certaines modifications n'ont pas été appliquées, ")
                    .append("vérifiez la plateforme et tentez de relancer le process à partir du endpoint" +
                            " \"resume\"");

            customMetrics.setStageMetric(70, message.toString());
        } else { customMetrics.setStageMetric(70); }

        try {
            serFile.delete();
            newMaps.serializeMaps(serFileName);
        } catch (IOException e) { log.error("Error during serialization"); }

        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.execute(extractBaseUrl + "/generate-extract", HttpMethod.POST, null, null);
        } catch (RestClientException e) {
            log.info("error when trying to generate extract, return message : {}", e.getLocalizedMessage());
            throw new ChangesApplicationException(e);
        }

    }


    private void handlePsCreateFailed(StringBuilder sb, Map<String, Professionnel> psMap) {
        addOperationHeader(sb, psMap, "Créations PS en échec : ");

        psMap.values().forEach(ps -> {
            appendOperationFailureInfos(sb, "PS", ps.getNationalId(), ps.getReturnStatus());
            if (is5xxError(ps.getReturnStatus())) {
                newMaps.getPsMap().remove(ps.getNationalId());
            }
        });
    }

    private void handlePsUpdateFailed(StringBuilder sb, Map<String, Professionnel> psMap) {
        addOperationHeader(sb, psMap, "Modifications PS en échec : ");

        psMap.values().forEach(ps -> {
            appendOperationFailureInfos(sb, "PS", ps.getNationalId(), ps.getReturnStatus());
            if (is5xxError(ps.getReturnStatus())) {
                newMaps.getPsMap().replace(ps.getNationalId(), oldMaps.getPsMap().get(ps.getNationalId()));
            }
        });
    }

    private void handlePsDeleteFailed(StringBuilder sb, Map<String, Professionnel> psMap) {
        addOperationHeader(sb, psMap, "Suppressions PS en échec : ");

        psMap.values().forEach(ps -> {
            appendOperationFailureInfos(sb, "PS", ps.getNationalId(), ps.getReturnStatus());
            if (is5xxError(ps.getReturnStatus())) {
                newMaps.getPsMap().put(ps.getNationalId(), ps);
            }
        });
    }

    private void handleStructureCreateFailed(StringBuilder sb, Map<String, Structure> structureMap) {
        addOperationHeader(sb, structureMap, "Créations Structure en échec : ");

        structureMap.values().forEach(structure -> {
            appendOperationFailureInfos(sb, "Structure", structure.getStructureTechnicalId(), structure.getReturnStatus());
            if (is5xxError(structure.getReturnStatus())) {
                newMaps.getStructureMap().remove(structure.getStructureTechnicalId());
            }

        });
    }

    private void handleStructureUpdateFailed(StringBuilder sb, Map<String, Structure> structureMap) {
        addOperationHeader(sb, structureMap, "Modifications Structure en échec : ");

        structureMap.values().forEach(structure -> {
            appendOperationFailureInfos(sb, "Structure", structure.getStructureTechnicalId(), structure.getReturnStatus());
            if (is5xxError(structure.getReturnStatus())) {
                newMaps.getStructureMap().replace(structure.getStructureTechnicalId(),
                        oldMaps.getStructureMap().get(structure.getStructureTechnicalId()));
            }

        });
    }

    private void addOperationHeader(StringBuilder sb, Map map, String header) {
        sb.append(header).append(map.size()).append(System.lineSeparator());
    }

    private void appendOperationFailureInfos(StringBuilder sb, String entityKind, String entityKey, int httpStatusCode) {
        sb.append(System.lineSeparator())
                .append(entityKind).append(" : ").append(entityKey).append(" ")
                .append("status code : ").append(httpStatusCode).append(System.lineSeparator());
    }

    private boolean is5xxError(int rawReturnStatus) {
        return HttpStatus.valueOf(rawReturnStatus).is5xxServerError();
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
