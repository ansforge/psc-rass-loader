/**
 * Copyright (C) 2022-2023 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class PsUpdateMap.
 */
public class PsUpdateMap extends OperationMap<String, RassEntity> {

	private Map<String , RassEntity> newValues;
	private Map<String, RassEntity> oldValues;

	/**
	 * Instantiates a new ps update map.
	 */
	public PsUpdateMap() {
		super();
	}

	/**
	 * Instantiates a new ps update map.
	 *
	 * @param operation the operation
	 */
	public PsUpdateMap(OperationType operation) {
		super(operation);

	}

	@Override
	public OperationType getOperation() {
		return OperationType.UPDATE;
	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
	}
}
