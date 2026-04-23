/*
 * Copyright © 2022-2026 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
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
 * Signals that the RASS endpoint has no new file to deliver (HTTP 503 per the
 * ANS contract). Distinct from {@link DownloadException} so the scheduler can
 * treat this as a benign "nothing to do" outcome instead of a failure.
 */
public class NoNewFileAvailableException extends DownloadException {

	private static final long serialVersionUID = 1L;

	public NoNewFileAvailableException(String message) {
		super(message);
	}
}
