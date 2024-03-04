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
package fr.ans.psc.pscload.state.exception;

/**
 * The Class UploadException.
 */
public class UploadException extends LoadProcessException {



	/**
	 * 
	 */
	private static final long serialVersionUID = -9145932753460359258L;

	/**
	 * Instantiates a new Upload exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public UploadException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new Upload exception.
	 *
	 * @param cause the cause
	 */
	public UploadException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new Upload exception.
	 */
	public UploadException() {
		super();
	}

	/**
	 * Instantiates a new Upload exception.
	 *
	 * @param message the message
	 */
	public UploadException(String message) {
		super(message);
	}

}
