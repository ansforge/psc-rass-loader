/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ans.psc.pscload.model.SerializableValueDifference;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.api.StructureApi;
import fr.ans.psc.model.Profession;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import fr.ans.psc.pscload.state.exception.UploadException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class UploadingChanges.
 */
@Slf4j
public class UploadingChanges extends ProcessState {


    private String[] excludedProfessions;

    private String apiBaseUrl;

    /**
     * Instantiates a new Uploading Changes.
     */
    public UploadingChanges() {
        super();

    }

    /**
     * Instantiates a new Uploading Changes.
     *
     * @param excludedProfessions the excluded professions
     * @param apiBaseUrl          the api base url
     */
    public UploadingChanges(String[] excludedProfessions, String apiBaseUrl) {
        this.excludedProfessions = excludedProfessions;
        this.apiBaseUrl = apiBaseUrl;

    }

    @Override
    public void nextStep() throws LoadProcessException {
        uploadPsToCreate(process.getPsToCreate());
        uploadPsToUpdate(process.getPsToUpdate());
        uploadPsToDelete(process.getPsToDelete());
        // Structures
        uploadStructuresToCreate(process.getStructureToCreate());
        uploadStructuresToUpdate(process.getStructureToUpdate());
    }

    @Override
	public void write(Kryo kryo, Output output) {
    	kryo.writeClassAndObject(output, excludedProfessions);
    	output.writeString(apiBaseUrl);
    }

    @Override
	public void read(Kryo kryo, Input input) {
        excludedProfessions = (String[]) kryo.readClassAndObject(input);
        apiBaseUrl = input.readString();
    }

    private void uploadStructuresToCreate(Map<String, Structure> structuresToCreate) throws LoadProcessException {
        ApiClient client = new ApiClient();
        client.setBasePath(apiBaseUrl);
        StructureApi structureapi = new StructureApi(client);
        structuresToCreate.values().parallelStream().forEach(structure -> {
            try {
                structureapi.createNewStructure(structure);
                // Remove structure from map if return code is 201
                structuresToCreate.remove(structure.getStructureTechnicalId());
            } catch (RestClientResponseException e) {
                log.error("error when creation of structure : {}, return code : {}", structure.getStructureTechnicalId(), e.getLocalizedMessage());
                structure.setReturnStatus(e.getRawStatusCode());
            } catch (RestClientException e) {
                log.error("error when creation of structure : {}, return message : {}", structure.getStructureTechnicalId(), e.getLocalizedMessage());
                throw new UploadException(e);
            }

        });
        log.info("structuresToCreate size is now {}", structuresToCreate.size() );
    }

    private void uploadStructuresToUpdate(Map<String, SerializableValueDifference<Structure>> structuresToUpdate) throws LoadProcessException {
        ApiClient client = new ApiClient();
        client.setBasePath(apiBaseUrl);
        StructureApi structureapi = new StructureApi(client);

        structuresToUpdate.values().parallelStream().forEach(structure -> {
            try {
                structureapi.updateStructure(structure.rightValue());
                // Remove entry if return code is 2xx
                structuresToUpdate.remove(structure.rightValue().getStructureTechnicalId());
            } catch (RestClientResponseException e) {
                log.error("error when update of structure : {}, return message : {}", structure.rightValue().getStructureTechnicalId(), e.getLocalizedMessage());
                structure.rightValue().setReturnStatus(e.getRawStatusCode());
            } catch (RestClientException e) {
                log.error("error when creation of structure : {}, return message : {}", structure.rightValue().getStructureTechnicalId(), e.getLocalizedMessage());
                throw new UploadException(e);
            }

        });
        	log.info("structuresToUpdate size is now {}", structuresToUpdate.size() );
    }

    private void uploadPsToCreate(Map<String, Professionnel> psToCreate) throws LoadProcessException {
        ApiClient client = new ApiClient();
        client.setBasePath(apiBaseUrl);
        PsApi psapi = new PsApi(client);
        psToCreate.values().parallelStream().forEach(ps -> {
            try {
                psapi.createNewPs(ps);
                // remove PS from map if status 2xx
                psToCreate.remove(ps.getNationalId());
            } catch (RestClientResponseException e) {
                log.error("error when creation of ps : {}, return code : {}", ps.getNationalId(), e.getLocalizedMessage());
                ps.setReturnStatus(e.getRawStatusCode());
            } catch (RestClientException e) {
                log.error("error when creation of ps : {}, return message : {}", ps.getNationalId(), e.getLocalizedMessage());
                throw new UploadException(e);
            }
        });
        log.info("psToCreate size is now {}", psToCreate.size() );
    }

    private void uploadPsToDelete(Map<String, Professionnel> psToDelete) throws LoadProcessException {
        ApiClient client = new ApiClient();
        client.setBasePath(apiBaseUrl);
        PsApi psapi = new PsApi(client);
        psToDelete.values().parallelStream().forEach(ps -> {
            List<Profession> psExPros = ps.getProfessions();
            AtomicBoolean deletable = new AtomicBoolean(true);

            psExPros.forEach(exerciceProfessionnel -> {
                if (excludedProfessions != null && Arrays.stream(excludedProfessions)
                        .anyMatch(profession -> exerciceProfessionnel.getCode().equals(profession))) {
                    deletable.set(false);
                }
            });

            if (deletable.get()) {
                try {
                    psapi.deletePsById(ps.getNationalId());
                    // remove PS from map if status 2xx
                    psToDelete.remove(ps.getNationalId());
                } catch (RestClientResponseException e) {
                    log.error("error when deletion of ps : {}, return message : {}", ps.getNationalId(), e.getLocalizedMessage());
                    ps.setReturnStatus(e.getRawStatusCode());
                } catch (RestClientException e) {
                    log.error("error when deletion of ps : {}, return message : {}", ps.getNationalId(), e.getLocalizedMessage());
                    throw new UploadException(e);
                }
            } else {
                // remove this PS to empty the list at the end of this stage
                psToDelete.remove(ps.getNationalId());
            }
        });
        
        log.info("psToDelete size is now {}", psToDelete.size() );
    }

    private void uploadPsToUpdate(Map<String, SerializableValueDifference<Professionnel>> psToUpdate) throws LoadProcessException {
        ApiClient client = new ApiClient();
        client.setBasePath(apiBaseUrl);
        PsApi psapi = new PsApi(client);

        psToUpdate.values().parallelStream().forEach(ps -> {
            try {
                psapi.updatePs(ps.rightValue());
                // remove PS from map if status 2xx
                psToUpdate.remove(ps.rightValue().getNationalId());
            } catch (RestClientResponseException e) {
                log.error("error when update of ps : {}, return message : {}", ps.rightValue().getNationalId(), e.getLocalizedMessage());
                ps.rightValue().setReturnStatus(e.getRawStatusCode());
            } catch (RestClientException e) {
                log.error("error when update of ps : {}, return message : {}", ps.rightValue().getNationalId(), e.getLocalizedMessage());
                throw new UploadException(e);
            }

        });
        log.info("psToUpdate size is now {}", psToUpdate.size() );
    }


}
