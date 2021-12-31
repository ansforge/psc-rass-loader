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
import fr.ans.psc.pscload.model.EmailTemplate;
import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.SerializableValueDifference;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.exception.ExtractTriggeringException;
import fr.ans.psc.pscload.state.exception.SerFileGenerationException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ChangesApplied.
 */
@Slf4j
public class ChangesApplied extends ProcessState {

    private CustomMetrics customMetrics;
    private String extractBaseUrl;

    public ChangesApplied(CustomMetrics customMetrics, String extractBaseUrl) {
        super();
        this.customMetrics = customMetrics;
        this.extractBaseUrl = extractBaseUrl;
        this.isAlreadyComputed = true;
    }

    public ChangesApplied() {
        super();
        this.isAlreadyComputed = true;
    }

    @Override
    public void nextStep() {
    	//First step
    	processRemainingPS();
    	// after this memory is cleared
        callPscExtract();
    }

	private void callPscExtract() throws ExtractTriggeringException{
		RestTemplate restTemplate = new RestTemplate();
		try {
			restTemplate.execute(extractBaseUrl + "/generate-extract", HttpMethod.POST, null, null);
		} catch (RestClientException e) {
			log.info("error when trying to generate extract, return message : {}", e.getLocalizedMessage());
	        throw new ExtractTriggeringException(e);
		}
	}

	private void processRemainingPS() throws SerFileGenerationException {
		MapsHandler newMaps = new MapsHandler();
        String lockedFilePath = process.getTmpMapsPath();
        String serFileName = new File(lockedFilePath).getParent() + File.separator + "maps.ser";
        File lockedSerFile = new File(lockedFilePath);
        File serFile = new File(serFileName);

        try {
        	newMaps.deserializeMaps(lockedFilePath);
        } catch (IOException | ClassNotFoundException e) {
            String msgLogged = e.getClass().equals(IOException.class) ? "Error during deserialization" : "Serialized file not found";
            log.error(msgLogged, e.getLocalizedMessage());
            throw new SerFileGenerationException(e);
        }

        try {
            if (process.isRemainingPsOrStructuresInMaps()) {
                StringBuilder message = new StringBuilder();
                addOperationHeader(message, process.getPsToCreate(), "Créations PS en échec : ");
                handlePsCreateFailed(message, process.getPsToCreate(), newMaps);
                addOperationHeader(message, process.getPsToDelete(), "Suppressions PS en échec : ");
                handlePsDeleteFailed(message, process.getPsToDelete(), newMaps);
                addOperationHeader(message, process.getPsToUpdate(), "Modifications PS en échec : ");
                handlePsUpdateFailed(message, process.getPsToUpdate(), newMaps);

                addOperationHeader(message, process.getStructureToCreate(), "Créations Structure en échec : ");
                handleStructureCreateFailed(message, process.getStructureToCreate(), newMaps);
                addOperationHeader(message, process.getStructureToCreate(), "Modifications Structure en échec : ");
                handleStructureUpdateFailed(message, process.getStructureToUpdate(), newMaps);

                message.append("Si certaines modifications n'ont pas été appliquées, ")
                        .append("vérifiez la plateforme et tentez de relancer le process à partir du endpoint" +
                                " \"resume\"");

                customMetrics.setStageMetric(70, EmailTemplate.UPLOAD_INCOMPLETE, message.toString());
            } else {
                customMetrics.setStageMetric(70, EmailTemplate.PROCESS_FINISHED,
                        "Le process PSCLOAD s'est terminé, le fichier " + process.getExtractedFilename() +
                                " a été correctement traité.");
            }
            serFile.delete();
            newMaps.serializeMaps(serFileName);
            lockedSerFile.delete();

            
        } catch (IOException e) {
            log.error("Error during serialization");
            throw new SerFileGenerationException("Error during serialization");
        }
	}

	private void handlePsCreateFailed(StringBuilder sb, Map<String, Professionnel> psMap, MapsHandler newMaps) {
        psMap.values().forEach(ps -> {
            appendOperationFailureInfos(sb, "PS", ps.getNationalId(), ps.getReturnStatus());
            if (is5xxError(ps.getReturnStatus())) {
                newMaps.getPsMap().remove(ps.getNationalId());
            }
        });
    }

    private void handlePsUpdateFailed(StringBuilder sb, Map<String, SerializableValueDifference<Professionnel>> psMap, MapsHandler newMaps) {
        psMap.values().forEach(ps -> {
            appendOperationFailureInfos(sb, "PS", ps.rightValue().getNationalId(), ps.rightValue().getReturnStatus());
            if (is5xxError(ps.rightValue().getReturnStatus())) {
                newMaps.getPsMap().replace(ps.rightValue().getNationalId(), ps.leftValue());
            }
        });
    }

    private void handlePsDeleteFailed(StringBuilder sb, Map<String, Professionnel> psMap, MapsHandler newMaps) {
        psMap.values().forEach(ps -> {
            appendOperationFailureInfos(sb, "PS", ps.getNationalId(), ps.getReturnStatus());
            if (is5xxError(ps.getReturnStatus())) {
                newMaps.getPsMap().put(ps.getNationalId(), ps);
            }
        });
    }

    private void handleStructureCreateFailed(StringBuilder sb, Map<String, Structure> structureMap, MapsHandler newMaps) {
        structureMap.values().forEach(structure -> {
            appendOperationFailureInfos(sb, "Structure", structure.getStructureTechnicalId(), structure.getReturnStatus());
            if (is5xxError(structure.getReturnStatus())) {
                newMaps.getStructureMap().remove(structure.getStructureTechnicalId());
            }

        });
    }

    private void handleStructureUpdateFailed(StringBuilder sb, Map<String, SerializableValueDifference<Structure>> structureMap, MapsHandler newMaps) {
        structureMap.values().forEach(structure -> {
            appendOperationFailureInfos(sb, "Structure", structure.rightValue().getStructureTechnicalId(), structure.rightValue().getReturnStatus());
            if (is5xxError(structure.rightValue().getReturnStatus())) {
                newMaps.getStructureMap().replace(structure.rightValue().getStructureTechnicalId(), structure.leftValue());
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
		output.writeString(extractBaseUrl);
	}

	@Override
	public void read(Kryo kryo, Input input) {
        extractBaseUrl = input.readString();
    }
}
