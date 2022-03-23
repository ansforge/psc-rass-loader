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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class PsCreateMap.
 */
public class PsCreateMap extends OperationMap<String, RassEntity> {

	private Map<String , RassEntity> newValues;

	/**
	 * Instantiates a new ps create map.
	 */
	public PsCreateMap() {
		super();
	}

	/**
	 * Instantiates a new ps create map.
	 *
	 * @param operation the operation
	 */
	public PsCreateMap(OperationType operation) {
		super(operation);

	}

	@Override
	public OperationType getOperation() {
		return OperationType.PS_CREATE;
	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
	}
}
