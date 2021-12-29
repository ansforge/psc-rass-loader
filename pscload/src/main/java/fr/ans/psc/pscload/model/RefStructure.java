/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model;

import fr.ans.psc.model.StructureRef;
import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class RefStructure extends StructureRef {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7056694315975238906L;

	/**
	 * Instantiates a new ref structure.
	 */
	public RefStructure() {
		super();
	}

	/**
	 * Instantiates a new ref structure.
	 *
	 * @param structureId the structure id
	 */
	public RefStructure(String structureId) {
		super();
		setStructureId(structureId);
	}

}
