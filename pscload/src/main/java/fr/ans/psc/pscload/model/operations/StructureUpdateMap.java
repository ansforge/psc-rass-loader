/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import java.util.List;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsCleanerVisitor;
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

	/**
	 * Instantiates a new structure update map.
	 *
	 * @param operation the operation
	 */
	public StructureUpdateMap(OperationType operation) {
		super(operation);

	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
	}

}
