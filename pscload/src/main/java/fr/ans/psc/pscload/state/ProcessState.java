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
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.KryoSerializable;

import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

/**
 * The Class ProcessState.
 */
public abstract class ProcessState implements KryoSerializable {
	
	/** The process. */
	protected LoadProcess process;

	public boolean isAlreadyComputed() {
		return false;
	}

	public boolean isExpirable() {
		return true;
	}

	/**
	 * Instantiates a new process state.
	 */
	// Constructor for deserializing process.
	public ProcessState() {}

	/**
	 * Run task.
	 *
	 * @throws LoadProcessException the load process exception
	 */
	abstract public void nextStep() throws LoadProcessException;
	
	public LoadProcess getProcess() {
		return process;
	}

	public void setProcess(LoadProcess process) {
		this.process = process;
	}

}
