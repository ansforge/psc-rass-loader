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
package fr.ans.psc.pscload.state.exception;

/**
 * The Class DiffException.
 */
public class DiffException extends LoadProcessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8223931257973919625L;

	/**
	 * Instantiates a new diff exception.
	 */
	public DiffException() {
		super();
	}

	/**
	 * Instantiates a new diff exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public DiffException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new diff exception.
	 *
	 * @param message the message
	 */
	public DiffException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new diff exception.
	 *
	 * @param cause the cause
	 */
	public DiffException(Throwable cause) {
		super(cause);
	}

}
