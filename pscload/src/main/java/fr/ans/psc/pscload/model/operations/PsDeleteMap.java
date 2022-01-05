/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import java.util.List;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsCleanerVisitor;
import fr.ans.psc.pscload.visitor.MapsUploaderVisitor;
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

	/**
	 * Instantiates a new ps delete map.
	 *
	 * @param operation the operation
	 */
	public PsDeleteMap(OperationType operation) {
		super(operation);

	}

	@Override
	public List<String> accept(MapsCleanerVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public void accept(MapsUploaderVisitor visitor) {
		visitor.visit(this);
		
	}

}
