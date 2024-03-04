/**
 * Copyright (C) 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscload.model.operations;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.Visitable;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class OperationMap.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@Getter
@Setter
public abstract class OperationMap<K, V> extends ConcurrentHashMap<String, RassEntity> implements Visitable {

	private OperationType operation;

	private ConcurrentMap<String, RassEntity> oldValues;

	private boolean locked;
	/**
	 * Instantiates a new operation map.
	 */
	public OperationMap() {
		super();
	}

	/**
	 * Instantiates a new operation map.
	 *
	 * @param operation the operation
	 */
	public OperationMap(OperationType operation) {
		super();
		this.operation = operation;
	}

	public abstract OperationType getOperation();

	/**
	 * Save old value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void saveOldValue(String key, RassEntity value) {
		if (oldValues == null) {
			oldValues = new ConcurrentHashMap<>();
		}
		oldValues.put(key, value);
	}

	/**
	 * Gets the old value.
	 *
	 * @param key the key
	 * @return the old value
	 */
	public RassEntity getOldValue(String key) {
		if (oldValues == null) {
			oldValues = new ConcurrentHashMap<>();
		}
		return oldValues.get(key);
	}

}
