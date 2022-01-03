/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.ans.psc.pscload.model.SerializableValueDifference;
import fr.ans.psc.pscload.model.EmailTemplate;
import fr.ans.psc.pscload.state.exception.ExtractTriggeringException;
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
import fr.ans.psc.pscload.state.exception.SerFileGenerationException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ChangesApplied.
 */
@Slf4j
public class ChangesApplied extends ProcessState {

    private CustomMetrics customMetrics;
    private String extractBaseUrl;
    private MapsHandler newMaps = new MapsHandler();

    private final String FAILURE_REPORT_FILENAME = "PSCLOAD_changements_en_échec.";

    public ChangesApplied(CustomMetrics customMetrics, String extractBaseUrl) {
        super();
        this.customMetrics = customMetrics;
        this.extractBaseUrl = extractBaseUrl;
    }

    @Override
    public boolean isAlreadyComputed() {
        return true;
    }

    @Override
    public void nextStep() {
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
                List<String> dataLines = new ArrayList<>();

                addOperationHeader(message, process.getPsToCreate(), "Créations PS en échec : ");
                handlePsCreateFailed(process.getPsToCreate(), dataLines);
                addOperationHeader(message, process.getPsToDelete(), "Suppressions PS en échec : ");
                handlePsDeleteFailed(process.getPsToDelete(), dataLines);
                addOperationHeader(message, process.getPsToUpdate(), "Modifications PS en échec : ");
                handlePsUpdateFailed(process.getPsToUpdate(), dataLines);

                addOperationHeader(message, process.getStructureToCreate(), "Créations Structure en échec : ");
                handleStructureCreateFailed(process.getStructureToCreate(), dataLines);
                addOperationHeader(message, process.getStructureToCreate(), "Modifications Structure en échec : ");
                handleStructureUpdateFailed(process.getStructureToUpdate(), dataLines);

                message.append("Si certaines modifications n'ont pas été appliquées, ")
                        .append("vérifiez la plateforme et tentez de relancer le process à partir du endpoint" +
                                " \"resume\"");

                DateFormat df = new SimpleDateFormat("yyyMMddhhmm");
                String now = df.format(new Date());
                File csvOutputFile = new File(serFile.getParent(), FAILURE_REPORT_FILENAME + now + ".csv");
                try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                    pw.println("Entité;identifiant;opération;Http status");
                    dataLines.stream().forEach(pw::println);
                }
                customMetrics.setStageMetric(70, EmailTemplate.UPLOAD_INCOMPLETE, message.toString(), csvOutputFile);
                csvOutputFile.delete();
            } else {
                customMetrics.setStageMetric(70, EmailTemplate.PROCESS_FINISHED,
                        "Le process PSCLOAD s'est terminé, le fichier " + process.getExtractedFilename() +
                                " a été correctement traité.", null);
            }
            serFile.delete();
            newMaps.serializeMaps(serFileName);
            lockedSerFile.delete();

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.execute(extractBaseUrl + "/generate-extract", HttpMethod.POST, null, null);

        } catch (IOException e) {
            log.error("Error during serialization");
            throw new SerFileGenerationException("Error during serialization");
        } catch (RestClientException e) {
            log.info("error when trying to generate extract, return message : {}", e.getLocalizedMessage());
            throw new ExtractTriggeringException(e);
        }
    }


    private void handlePsCreateFailed(Map<String, Professionnel> psMap, List<String> dataLines) {
        psMap.values().forEach(ps -> {
            dataLines.add(appendOperationFailureInfos("PS", ps.getNationalId(), ps.getReturnStatus(), "create"));
            if (is5xxError(ps.getReturnStatus())) {
                newMaps.getPsMap().remove(ps.getNationalId());
            }
        });
    }

    private void handlePsUpdateFailed(Map<String, SerializableValueDifference<Professionnel>> psMap, List<String> dataLines) {
        psMap.values().forEach(ps -> {
            dataLines.add(appendOperationFailureInfos("PS", ps.rightValue().getNationalId(), ps.rightValue().getReturnStatus(), "update"));
            if (is5xxError(ps.rightValue().getReturnStatus())) {
                newMaps.getPsMap().replace(ps.rightValue().getNationalId(), ps.leftValue());
            }
        });
    }

    private void handlePsDeleteFailed(Map<String, Professionnel> psMap, List<String> dataLines) {
        psMap.values().forEach(ps -> {
            dataLines.add(appendOperationFailureInfos("PS", ps.getNationalId(), ps.getReturnStatus(), "delete"));
            if (is5xxError(ps.getReturnStatus())) {
                newMaps.getPsMap().put(ps.getNationalId(), ps);
            }
        });
    }

    private void handleStructureCreateFailed(Map<String, Structure> structureMap, List<String> dataLines) {
        structureMap.values().forEach(structure -> {
            dataLines.add(appendOperationFailureInfos("Structure", structure.getStructureTechnicalId(), structure.getReturnStatus(), "create"));
            if (is5xxError(structure.getReturnStatus())) {
                newMaps.getStructureMap().remove(structure.getStructureTechnicalId());
            }

        });
    }

    private void handleStructureUpdateFailed(Map<String, SerializableValueDifference<Structure>> structureMap, List<String> dataLines) {
        structureMap.values().forEach(structure -> {
            dataLines.add(appendOperationFailureInfos("Structure", structure.rightValue().getStructureTechnicalId(),
                    structure.rightValue().getReturnStatus(), "update"));
            if (is5xxError(structure.rightValue().getReturnStatus())) {
                newMaps.getStructureMap().replace(structure.rightValue().getStructureTechnicalId(), structure.leftValue());
            }

        });
    }

    private void addOperationHeader(StringBuilder sb, Map map, String header) {
        sb.append(header).append(map.size()).append(System.lineSeparator());
    }

    private String appendOperationFailureInfos(String entityKind, String entityKey, int httpStatusCode, String operation) {
        String[] dataItems = new String[] {entityKind, entityKey, operation, String.valueOf(httpStatusCode)};
        return String.join(";", dataItems);
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
