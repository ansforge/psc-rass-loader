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
package fr.ans.psc.pscload.visitor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.model.Profession;
import fr.ans.psc.pscload.metrics.CustomMetrics.ID_TYPE;
import fr.ans.psc.pscload.metrics.CustomMetrics.SizeMetric;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationType;
import fr.ans.psc.pscload.model.operations.PsCreateMap;
import fr.ans.psc.pscload.model.operations.PsDeleteMap;
import fr.ans.psc.pscload.model.operations.PsUpdateMap;
import fr.ans.psc.pscload.service.MessageProducer;
import fr.ans.psc.pscload.state.exception.LockedMapException;
import fr.ans.psc.pscload.state.exception.UploadException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class MapsUploaderVisitorImpl.
 */
@Slf4j
public class MapsUploaderVisitorImpl implements MapsVisitor {

	private String[] excludedProfessions;

	private PsApi psApi;

	private MessageProducer messageProducer;

	private boolean messagesEnabled = !Boolean.getBoolean("disable.messages");

	private final Map<SizeMetric, AtomicInteger> effectiveCounts = new EnumMap<>(SizeMetric.class);

	private final Map<String, AtomicInteger> failureCounts = new LinkedHashMap<>();

	/**
	 * Instantiates a new maps uploader visitor impl.
	 *
	 * @param excludedProfessions the excluded professions
	 * @param apiBaseUrl          the api base url
	 */
	public MapsUploaderVisitorImpl(String[] excludedProfessions, String apiBaseUrl, MessageProducer messageProducer) {
		super();
		this.excludedProfessions = excludedProfessions;
		ApiClient apiClient = new ApiClient();
		apiClient.setBasePath(apiBaseUrl);
		this.psApi = new PsApi(apiClient);
		this.messageProducer = messageProducer;
		Arrays.stream(ID_TYPE.values()).forEach(idType -> {
			for (String op : new String[] { "CREATE", "UPDATE", "DELETE" }) {
				effectiveCounts.put(SizeMetric.valueOf(op + "_" + idType.name() + "_SIZE"), new AtomicInteger(0));
			}
		});
	}

	/**
	 * Returns the count of operations actually applied on the ps-api side, keyed by
	 * {@link SizeMetric}. Differs from the diff prediction when a predicted CREATE
	 * (POST) already exists on server and is served as a partial update (HTTP 200);
	 * these are counted as UPDATE.
	 */
	public Map<SizeMetric, Integer> getEffectiveCounts() {
		Map<SizeMetric, Integer> snapshot = new EnumMap<>(SizeMetric.class);
		effectiveCounts.forEach((k, v) -> snapshot.put(k, v.get()));
		return snapshot;
	}

	/**
	 * Returns the failure count per "OPERATION_IDTYPE_HTTPSTATUS" key (e.g.
	 * "CREATE_RPPS_500").
	 */
	public Map<String, Integer> getFailureCounts() {
		Map<String, Integer> snapshot = new LinkedHashMap<>();
		failureCounts.forEach((k, v) -> snapshot.put(k, v.get()));
		return snapshot;
	}

	private ID_TYPE idTypeOf(Professionnel ps) {
		String raw = ps.getIdType();
		if (raw == null) {
			return null;
		}
		for (ID_TYPE t : ID_TYPE.values()) {
			if (t.value.equals(raw)) {
				return t;
			}
		}
		return null;
	}

	private void incrementEffective(OperationType effectiveOp, Professionnel ps) {
		ID_TYPE type = idTypeOf(ps);
		if (type == null) {
			return;
		}
		SizeMetric key = SizeMetric.valueOf(effectiveOp.name() + "_" + type.name() + "_SIZE");
		effectiveCounts.get(key).incrementAndGet();
	}

	private void incrementFailure(OperationType predictedOp, Professionnel ps, int httpStatus) {
		ID_TYPE type = idTypeOf(ps);
		String typeName = type == null ? "UNKNOWN" : type.name();
		String key = predictedOp.name() + "_" + typeName + "_" + httpStatus;
		failureCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void visit(PsCreateMap map) {
		Collection<RassEntity> items = map.values();
		items.stream().forEach(item -> {
			try {
				if (map.isLocked()) {
					log.info("Map is locked during shutdown");
					long id = 0L;
					try {
						id = Thread.currentThread().getId();
						Thread.currentThread().stop();
					} catch (ThreadDeath ignore) {
						log.info("Thread {} is stopped", id);
					}
					throw new LockedMapException();
				}
				Professionnel ps = (Professionnel) item;
				log.info("{} -> CREATE", ps.getNationalId());
				ResponseEntity<Void> response = psApi.createNewPsWithHttpInfo(ps);
				HttpStatus status = response.getStatusCode();
				OperationType effectiveOp = status == HttpStatus.CREATED ? OperationType.CREATE : OperationType.UPDATE;
				incrementEffective(effectiveOp, ps);
				if (status != HttpStatus.CREATED && status != HttpStatus.OK) {
					log.warn("Unexpected HTTP status on CREATE {} : {}", ps.getNationalId(), status);
				}
				map.remove(item.getInternalId());
				if (messagesEnabled) {
					messageProducer.sendPsMessage(ps, effectiveOp);
				}
			} catch (RestClientResponseException e) {
				log.error("error when {} : {}, return code : {}", map.getOperation().toString(), item.getInternalId(),
						e.getLocalizedMessage());
				item.setReturnStatus(e.getRawStatusCode());
				incrementFailure(OperationType.CREATE, (Professionnel) item, e.getRawStatusCode());
			} catch (RestClientException e) {
				log.error("error when {} : {}, return message : {}", map.getOperation().toString(),
						item.getInternalId(), e.getLocalizedMessage());
				throw new UploadException(e);
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException();
			}
		});

	}

	@SuppressWarnings("deprecation")
	@Override
	public void visit(PsDeleteMap map) {
		Collection<RassEntity> items = map.values();
		items.stream().forEach(item -> {
			try {
				if (map.isLocked()) {
					log.info("Map is locked during shutdown");
					long id = 0L;
					try {
						id = Thread.currentThread().getId();
						Thread.currentThread().stop();
					} catch (ThreadDeath ignore) {
						log.info("Thread {} is stopped", id);
					}
					throw new LockedMapException();
				}
				Professionnel prof = (Professionnel) item;
				String nationalId = prof.getNationalId() != null ? prof.getNationalId() : prof.getId();
				log.info("{} -> DELETE", nationalId);
				List<Profession> psExPros = prof.getProfessions();
				AtomicBoolean deletable = new AtomicBoolean(true);
				if (psExPros != null) {
					psExPros.forEach(exerciceProfessionnel -> {
						try {
							if (exerciceProfessionnel != null && exerciceProfessionnel.getCode() != null 
									&& !exerciceProfessionnel.getCode().isEmpty() && excludedProfessions != null) {
								for (String excludedCode : excludedProfessions) {
									if (excludedCode != null && excludedCode.equals(exerciceProfessionnel.getCode())) {
										deletable.set(false);
										break;
									}
								}
							}
						} catch (Exception e) {
							log.error("Error checking profession for PS {}: {}", nationalId, e.getMessage(), e);
						}
					});
				}
				if (deletable.get()) {
					String internalId = item.getInternalId();
					if (internalId != null) {
						ResponseEntity<Void> response = psApi.deletePsByIdWithHttpInfo(
								URLEncoder.encode(internalId, StandardCharsets.UTF_8));
						if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
							log.warn("Unexpected HTTP status on DELETE {} : {}", nationalId, response.getStatusCode());
						}
						incrementEffective(OperationType.DELETE, prof);
					} else {
						log.warn("PS {} has null internalId, cannot delete", nationalId);
					}
				}
				// remove anyway : extract Ps from maps either successful or ignored
				String internalId = item.getInternalId();
				if (internalId != null) {
					map.remove(internalId);
				}
				if (messagesEnabled && deletable.get()) {
					try {
						messageProducer.sendPsMessage((Professionnel) item, OperationType.DELETE);
					} catch (Exception e) {
						log.error("Error sending DELETE message for PS {}: {}", nationalId, e.getMessage(), e);
					}
				}
			} catch (RestClientResponseException e) {
				log.error("error when {} : {}, return code : {}", map.getOperation().toString(), item.getInternalId(),
						e.getLocalizedMessage());
				item.setReturnStatus(e.getRawStatusCode());
				incrementFailure(OperationType.DELETE, (Professionnel) item, e.getRawStatusCode());
			} catch (RestClientException e) {
				log.error("error when {} : {}, return message : {}", map.getOperation().toString(),
						item.getInternalId(), e.getLocalizedMessage());
				throw new UploadException(e);
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException();
			}
		});

	}

	@SuppressWarnings("deprecation")
	@Override
	public void visit(PsUpdateMap map) {
		log.info("MapsUploaderVisitorImpl.visit(PsUpdateMap map)");
		log.info("map.size()" + map.size());
		Collection<RassEntity> items = map.values();
		items.stream().forEach(item -> {
			try {
				if (map.isLocked()) {
					log.info("Map is locked during shutdown");
					long id = 0L;
					try {
						id = Thread.currentThread().getId();
						Thread.currentThread().stop();
					} catch (ThreadDeath ignore) {
						log.info("Thread {} is stopped", id);
					}
					throw new LockedMapException();
				}
				Professionnel ps = (Professionnel) item;
				log.info("{} -> UPDATE", ps.getNationalId());
				ResponseEntity<Void> response = psApi.updatePsWithHttpInfo(ps);
				if (response.getStatusCode() != HttpStatus.OK) {
					log.warn("Unexpected HTTP status on UPDATE {} : {}", ps.getNationalId(), response.getStatusCode());
				}
				incrementEffective(OperationType.UPDATE, ps);
				map.remove(item.getInternalId());
				map.getOldValues().remove(item.getInternalId());
				if (messagesEnabled) {
					messageProducer.sendPsMessage(ps, OperationType.UPDATE);
				}
			} catch (RestClientResponseException e) {
				log.error("error when {} : {}, return code : {}", map.getOperation().toString(), item.getInternalId(),
						e.getLocalizedMessage());
				item.setReturnStatus(e.getRawStatusCode());
				incrementFailure(OperationType.UPDATE, (Professionnel) item, e.getRawStatusCode());
			} catch (RestClientException e) {
				log.error("error when {} : {}, return message : {}", map.getOperation().toString(),
						item.getInternalId(), e.getLocalizedMessage());
				throw new UploadException(e);
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException();
			}
		});

	}

}
