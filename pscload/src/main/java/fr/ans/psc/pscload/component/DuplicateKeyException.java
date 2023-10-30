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
package fr.ans.psc.pscload.component;

/**
 * The Class DuplicateKeyException.
 */
public class DuplicateKeyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2055488226238329069L;

	/**
	 * Instantiates a new duplicate key exception.
	 */
	public DuplicateKeyException() {
		super();
	}

	/**
	 * Instantiates a new duplicate key exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public DuplicateKeyException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new duplicate key exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public DuplicateKeyException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new duplicate key exception.
	 *
	 * @param message the message
	 */
	public DuplicateKeyException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new duplicate key exception.
	 *
	 * @param cause the cause
	 */
	public DuplicateKeyException(Throwable cause) {
		super(cause);
	}

}
