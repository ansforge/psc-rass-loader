/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

/**
 * The Class PsDeleteMap.
 */
public class PsDeleteMap extends OperationMap<String, RassEntity> {

	/**
	 * Instantiates a new ps delete map.
	 */
	public PsDeleteMap() {
		super();

	}

	@Override
	public OperationType getOperation() {
		return OperationType.PS_DELETE;
	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
	}

}
