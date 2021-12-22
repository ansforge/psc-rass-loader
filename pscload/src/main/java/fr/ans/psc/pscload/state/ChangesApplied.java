/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.exception.ChangesApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.util.Map;

import static com.google.common.io.Files.move;

/**
 * The Class ChangesApplied.
 */
@Slf4j
public class ChangesApplied extends ProcessState {

    /**
     *
     */
    private static final long serialVersionUID = -2486351862090505174L;

    private CustomMetrics customMetrics;

    public ChangesApplied(CustomMetrics customMetrics) {
        this.customMetrics = customMetrics;
    }

    public ChangesApplied() {
    }

    private MapsHandler newMaps = new MapsHandler();
    private MapsHandler oldMaps = new MapsHandler();

    @Override
    public void runTask() {

        deserializeMaps(newMaps, process.getTmpMapsPath());
        File tmpSerFile = new File(process.getTmpMapsPath());

        String serFileName = tmpSerFile.getParent() + File.separator + "maps.ser";
        File serFile = new File(serFileName);
        if (serFile.exists()) {
            deserializeMaps(oldMaps, serFileName);
        }

        StringBuilder message = new StringBuilder();

        message.append("Créations PS en échec :");
        message.append(System.lineSeparator());
        process.getPsToCreate().values().stream().forEach(ps -> {
            appendOperationFailureInfos(message, "PS", ps.getNationalId(), ps.getReturnStatus());
            removePsIfClientError(process.getPsToCreate(), ps);
        });

        message.append("Suppressions PS en échec :");
        message.append(System.lineSeparator());
        process.getPsToDelete().values().stream().forEach(ps -> {
            appendOperationFailureInfos(message, "PS", ps.getNationalId(), ps.getReturnStatus());
            removePsIfClientError(process.getPsToDelete(), ps);
        });

        message.append("Modifications PS en échec :");
        message.append(System.lineSeparator());
        process.getPsToUpdate().values().stream().forEach(psDiff -> {
            appendOperationFailureInfos(message, "PS", psDiff.rightValue().getNationalId(), psDiff.rightValue().getReturnStatus());
            if (HttpStatus.valueOf(psDiff.rightValue().getReturnStatus()).is4xxClientError()) {
                process.getPsToUpdate().remove(psDiff.rightValue().getNationalId());
            }
        });

        message.append("Créations Structure en échec :");
        message.append(System.lineSeparator());
        process.getStructureToCreate().values().stream().forEach(structure -> {
            appendOperationFailureInfos(message, "Structure", structure.getStructureTechnicalId(), structure.getReturnStatus());
            removeStructureIfClientError(process.getStructureToCreate(), structure);
        });

        message.append("Suppressions Structure en échec :");
        message.append(System.lineSeparator());
        process.getStructureToDelete().values().stream().forEach(structure -> {
            appendOperationFailureInfos(message, "Structure", structure.getStructureTechnicalId(), structure.getReturnStatus());
            removeStructureIfClientError(process.getStructureToDelete(), structure);
        });

        message.append("Modifications Structure en échec :");
        message.append(System.lineSeparator());
        process.getStructureToUpdate().values().stream().forEach(structureDiff -> {
            appendOperationFailureInfos(message, "Structure", structureDiff.rightValue().getStructureTechnicalId(),
                    structureDiff.rightValue().getReturnStatus());
            if (HttpStatus.valueOf(structureDiff.rightValue().getReturnStatus()).is4xxClientError()) {
                process.getStructureToUpdate().remove(structureDiff.rightValue().getStructureTechnicalId());
            }
        });

        message.append("Si certaines modifications n'ont pas été appliquées, ");
        message.append("vérifiez la plateforme et tentez de relancer le process à partir du endpoint \"resume\"");


        // TODO remove failures from newMaps

        // TODO serialize newMaps
        serFile.delete();
        tmpSerFile.renameTo(serFile);



        if (process.isRemainingPsOrStructuresInMaps()) {
//            String message = buildMessageBody(process);
//            customMetrics.setStageMetric(40, message.toString());
        } else {
            customMetrics.setStageMetric(40);
        }

        // TODO call pscextract and deregister process now because we can't know the status of pscextract.
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO save metrics

    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO restore metrics

    }

    private void deserializeMaps(MapsHandler mapsToDeserializeHandler, String serializedFileName) throws ChangesApplicationException {
        try {
            FileInputStream fileInputStream = new FileInputStream(serializedFileName);
            ObjectInputStream ois = new ObjectInputStream(fileInputStream);
            mapsToDeserializeHandler.readExternal(ois);
            ois.close();
        } catch (IOException ioe) {
            throw new ChangesApplicationException("Error during I/O", ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new ChangesApplicationException("File " + serializedFileName + " has not been found", cnfe);
        }

    }

    private void appendOperationFailureInfos(StringBuilder sb, String entityKind, String entityKey, int httpStatusCode) {
        sb.append(System.lineSeparator());
        sb.append(entityKind + " : " + entityKey + " ");
        sb.append("status code : " + httpStatusCode);
        sb.append(System.lineSeparator());
    }

    private void removePsIfClientError(Map<String, Professionnel> psMap, Professionnel ps) {
        if (HttpStatus.valueOf(ps.getReturnStatus()).is4xxClientError()) {
            psMap.remove(ps.getNationalId());
        }
    }

    private void removeStructureIfClientError(Map<String, Structure> structureMap, Structure structure) {
        if (HttpStatus.valueOf(structure.getReturnStatus()).is4xxClientError()) {
            structureMap.remove(structure.getStructureTechnicalId());
        }
    }
}
