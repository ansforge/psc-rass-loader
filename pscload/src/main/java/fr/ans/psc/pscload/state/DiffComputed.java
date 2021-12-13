package fr.ans.psc.pscload.state;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.web.client.RestClientException;

import com.google.common.collect.MapDifference.ValueDifference;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.api.StructureApi;
import fr.ans.psc.model.Profession;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiffComputed extends ProcessState {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5204972581902084236L;

	private String[] excludedProfessions;

	private String apiBaseUrl;

	public DiffComputed() {
		super();

	}

	public DiffComputed(String[] excludedProfessions, String apiBaseUrl) {
		this.excludedProfessions = excludedProfessions;
		this.apiBaseUrl = apiBaseUrl;

	}

	@Override
	public void runTask() throws LoadProcessException {
		// TODOc check the case of exception
		uploadPsToCreate(process.getPsToCreate());
		uploadPsToUpdate(process.getPsToUpdate());
		uploadPsToDelete(process.getPsToDelete());
		// Structures
		uploadStructuresToCreate(process.getStructureToCreate());
		uploadStructuresToUpdate(process.getStructureToUpdate());
		//TODO log the entries still in the maps and clear the map.
		
		// TODO delete structures ?

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(excludedProfessions);
		out.writeObject(apiBaseUrl);

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		excludedProfessions = (String[]) in.readObject();
		apiBaseUrl = (String) in.readObject();

	}

	private void uploadStructuresToCreate(Map<String, Structure> structuresToCreate) throws LoadProcessException {
		ApiClient client = new ApiClient();
		client.setBasePath(apiBaseUrl);
		StructureApi structureapi = new StructureApi(client);
		structuresToCreate.values().parallelStream().forEach(structure -> {
			try {
				structureapi.createNewStructure(structure);
				// Remove structure from map if return code is 201
				// TODO Map is unmodifiable, check for another solution
				structuresToCreate.remove(structure.getStructureTechnicalId());
			} catch (RestClientException e) {
				log.error("error when creation of structure : {}, return code : {}", structure.getStructureTechnicalId(), e.getLocalizedMessage());
			}

		});
		
	}
	
	private void uploadStructuresToUpdate(Map<String, ValueDifference<Structure>> structuresToUpdate) throws LoadProcessException {
		ApiClient client = new ApiClient();
		client.setBasePath(apiBaseUrl);
		StructureApi structureapi = new StructureApi(client);

		structuresToUpdate.values().parallelStream().forEach(v -> {
			// TODO check if it is the good data !
			try {
				structureapi.updateStructure(v.rightValue());
				// Remove entry if return code is 200
				// TODO Map is unmodifiable, check for another solution
				structuresToUpdate.remove(v.rightValue().getStructureTechnicalId());
			} catch (RestClientException e) {
				log.error("error when update of structure : {}, return code : {}", v.rightValue().getStructureTechnicalId(), e.getLocalizedMessage());
			}

		});
		
	}

	private void uploadPsToCreate(Map<String, Professionnel> psToCreate) throws LoadProcessException {
		ApiClient client = new ApiClient();
		client.setBasePath(apiBaseUrl);
		PsApi psapi = new PsApi(client);
		psToCreate.values().parallelStream().forEach(ps -> {
		try {
			psapi.createNewPs(ps);
			// remove PS from map if status 201
			// TODO Map is unmodifiable, check for another solution
			psToCreate.remove(ps.getNationalId());
		} catch (RestClientException e) {
			log.error("error when creation of ps : {}, return code : {}", ps.getNationalId(), e.getLocalizedMessage());
		}
	});

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
						// remove PS from map if status 200
						// TODO Map is unmodifiable, check for another solution
						// diff.entriesOnlyOnLeft().remove(ps.getNationalId());
					} catch (RestClientException e) {
						log.error("error when deletion of ps : {}, return code : {}", ps.getNationalId(), e.getLocalizedMessage());
					}
				}
			});
	}
			
			
			
		private void uploadPsToUpdate(Map<String, ValueDifference<Professionnel>> psToUpdate) throws LoadProcessException {
			ApiClient client = new ApiClient();
			client.setBasePath(apiBaseUrl);
			PsApi psapi = new PsApi(client);

			psToUpdate.values().parallelStream().forEach(v -> {
			try {
				psapi.updatePs(v.rightValue());
				// remove PS from map if status 200
				// TODO Map is unmodifiable, check for another solution
				psToUpdate.remove(v.rightValue().getNationalId());
			} catch (RestClientException e) {
				log.error("error when update of ps : {}, return code : {}", v.rightValue().getNationalId(), e.getLocalizedMessage());
			}

		});
	}

}
