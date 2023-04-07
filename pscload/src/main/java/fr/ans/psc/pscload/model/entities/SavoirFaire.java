/*
 * Copyright A.N.S 2021
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
