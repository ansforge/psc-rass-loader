/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

/**
 * The Class PsUpdateMap.
 */
public class PsUpdateMap extends OperationMap<String, RassEntity> {

	/**
	 * Instantiates a new ps update map.
	 */
	public PsUpdateMap() {
		super();

	}

	@Override
	public OperationType getOperation() {
		return OperationType.PS_UPDATE;
	}


	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
	}
}
