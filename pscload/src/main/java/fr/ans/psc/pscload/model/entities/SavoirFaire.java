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
package fr.ans.psc.pscload.model.entities;

import fr.ans.psc.model.Expertise;
import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class SavoirFaire extends Expertise {

    /**
     *
     */
    private static final long serialVersionUID = 8385751071373736733L;

    /**
     * Instantiates a new savoir faire.
     */
    public SavoirFaire() {
        super();
    }

    /**
     * Instantiates a new savoir faire.
     *
     * @param items the items
     */
    public SavoirFaire(String[] items) {
        super();
        setTypeCode(items[RassItems.EXPERTISE_TYPE_CODE.column]);
        setCode(items[RassItems.EXPERTISE_CODE.column]);
    }

    public SavoirFaire(Expertise expertise) {
        super();
        setTypeCode(expertise.getTypeCode());
        setCode(expertise.getCode());
    }

    public void setSavoirFaireItems(String[] items) {
		items[RassItems.EXPERTISE_TYPE_CODE.column] = getTypeCode();
		items[RassItems.EXPERTISE_CODE.column] = getCode();
	}
}
