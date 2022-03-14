/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

/**
 * The Class StructureUpdateMap.
 */
public class StructureUpdateMap extends OperationMap<String, RassEntity> {

	/**
	 * Instantiates a new structure update map.
	 */
	public StructureUpdateMap() {
		super();

	}

	@Override
	public OperationType getOperation() {
		return OperationType.STRUCTURE_UPDATE;
	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
	}

}
