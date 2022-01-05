package fr.ans.psc.pscload.visitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.api.StructureApi;
import fr.ans.psc.model.Profession;
import fr.ans.psc.pscload.model.OperationMap;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.RassEntity;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.exception.UploadException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapsUploaderVisitorImpl implements MapsUploaderVisitor {

	private String[] excludedProfessions;

	private StructureApi structureApi;

	private PsApi psApi;

	public MapsUploaderVisitorImpl(String[] excludedProfessions, String apiBaseUrl) {
		super();
		this.excludedProfessions = excludedProfessions;
		ApiClient apiClient = new ApiClient();
		apiClient.setBasePath(apiBaseUrl);
		this.structureApi = new StructureApi(apiClient);
		this.psApi = new PsApi(apiClient);
	}

	@Override
	public void visit(OperationMap<String, RassEntity> map) {
		Collection<RassEntity> items = map.values();
		items.parallelStream().forEach(item -> {
			try {
				switch (map.getOperation()) {
				case PS_CREATE:
					psApi.createNewPs((Professionnel) item);
					map.remove(item.getInternalId());
					break;
				case PS_UPDATE:
					psApi.updatePs((Professionnel) item);
					map.remove(item.getInternalId());
					break;
				case PS_DELETE:
					Professionnel prof = (Professionnel) item;
					List<Profession> psExPros = prof.getProfessions();
					AtomicBoolean deletable = new AtomicBoolean(true);
					psExPros.forEach(exerciceProfessionnel -> {
						if (excludedProfessions != null && Arrays.stream(excludedProfessions)
								.anyMatch(profession -> exerciceProfessionnel.getCode().equals(profession))) {
							deletable.set(false);
						}
					});
					if (deletable.get()) {
						psApi.deletePsById(item.getInternalId());
						// remove PS from map if status 2xx
						map.remove(item.getInternalId());
					}
					break;
				case STRUCTURE_CREATE:
					structureApi.createNewStructure((Structure) item);
					// Remove structure from map if return code is 201
					map.remove(item.getInternalId());
					break;
				case STRUCTURE_UPDATE:
					structureApi.updateStructure((Structure) item);
					map.remove(item.getInternalId());
				default:
					break;
				}
			} catch (RestClientResponseException e) {
				log.error("error when {} : {}, return code : {}", map.getOperation().toString(), item.getInternalId(),
						e.getLocalizedMessage());
				item.setReturnStatus(e.getRawStatusCode());
			} catch (RestClientException e) {
				log.error("error when {} : {}, return message : {}", map.getOperation().toString(),
						item.getInternalId(), e.getLocalizedMessage());
				throw new UploadException(e);
			}
		});

	}

}
