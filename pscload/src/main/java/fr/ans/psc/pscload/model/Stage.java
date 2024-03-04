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
package fr.ans.psc.pscload.model;

/**
 * The Enum Stage.
 */
public enum Stage {
    
    /** The finished. */
    FINISHED(0),
    
    /** The submitted. */
    SUBMITTED(10),
    
    /** The ready to extract. */
    READY_TO_EXTRACT(20),
    
    /** The ready to compute. */
    READY_TO_COMPUTE(30),
    
    /** The diff computed. */
    DIFF_COMPUTED(50),
    
    /** The upload changes started. */
    UPLOAD_CHANGES_STARTED(60),
    
    /** The upload changes finished. */
    UPLOAD_CHANGES_FINISHED(70),
    
    /** The current map serialized. */
    CURRENT_MAP_SERIALIZED(80);

    /** The value. */
    public int value;

    /**
     * Instantiates a new stage.
     *
     * @param value the value
     */
    Stage(int value) { this.value = value; }
}
