/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

/**
 * The Class StructureCreateMap.
 */
public class StructureCreateMap extends OperationMap<String, RassEntity> {

	/**
	 * Instantiates a new structure create map.
	 */
	public StructureCreateMap() {
		super();

	}

	/**
	 * Instantiates a new structure create map.
	 *
	 * @param operation the operation
	 */
	public StructureCreateMap(OperationType operation) {
		super(operation);

	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
	}

}
