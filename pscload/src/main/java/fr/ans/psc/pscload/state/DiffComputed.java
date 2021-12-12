package fr.ans.psc.pscload.state;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.web.client.RestClientException;

import com.google.common.collect.MapDifference;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.api.StructureApi;
import fr.ans.psc.model.Profession;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import fr.ans.psc.pscload.state.exception.UploadChangesException;

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
		uploadPsChanges(process.getPsMap());
		uploadStructuresChanges(process.getStructureMap());

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

	private void uploadStructuresChanges(MapDifference<String, Structure> diff) throws LoadProcessException {
		ApiClient client = new ApiClient();
		client.setBasePath(apiBaseUrl);
		try {
			StructureApi structureapi = new StructureApi(client);
			diff.entriesOnlyOnRight().values().parallelStream().forEach(structure -> {
				structureapi.createNewStructure(structure);
			});
			diff.entriesDiffering().values().parallelStream().forEach(v -> {
				structureapi.updateStructure(v.leftValue());
			});
			// TODO delete ?
		} catch (RestClientException e) {
			throw new UploadChangesException("Http error when uploading ps changes", e);
		}
	}

	private void uploadPsChanges(MapDifference<String, Professionnel> diff) throws LoadProcessException {
		ApiClient client = new ApiClient();
		client.setBasePath(apiBaseUrl);
		try {
			PsApi psapi = new PsApi(client);
			diff.entriesOnlyOnLeft().values().parallelStream().forEach(ps -> {
				List<Profession> psExPros = ps.getProfessions();
				AtomicBoolean deletable = new AtomicBoolean(true);

				psExPros.forEach(exerciceProfessionnel -> {
					if (excludedProfessions != null && Arrays.stream(excludedProfessions)
							.anyMatch(profession -> exerciceProfessionnel.getCode().equals(profession))) {
						deletable.set(false);
					}
				});

				if (deletable.get()) {
					psapi.deletePsById(ps.getNationalId());
				}
			});

			diff.entriesOnlyOnRight().values().parallelStream().forEach(ps -> {
				psapi.createNewPs(ps);
			});
			diff.entriesDiffering().values().parallelStream().forEach(v -> {
				psapi.updatePs(v.rightValue());
			});
		} catch (RestClientException e) {
			throw new UploadChangesException("Http error when uploading structures changes", e);
		}
	}

}
