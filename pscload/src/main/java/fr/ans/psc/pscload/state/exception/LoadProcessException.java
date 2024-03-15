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
package fr.ans.psc.pscload.state.exception;

/**
 * The Class LoadProcessException.
 */
public class LoadProcessException extends RuntimeException {



	/**
	 * Instantiates a new load process exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public LoadProcessException(String message, Throwable cause) {
		super(message, cause);
	}


	/**
	 * Instantiates a new load process exception.
	 *
	 * @param cause the cause
	 */
	public LoadProcessException(Throwable cause) {
		super(cause);
	}


	/**
	 * Instantiates a new load process exception.
	 */
	public LoadProcessException() {
		super();
	}


	/**
	 * Instantiates a new load process exception.
	 *
	 * @param message the message
	 */
	public LoadProcessException(String message) {
		super(message);
	}

	
}
