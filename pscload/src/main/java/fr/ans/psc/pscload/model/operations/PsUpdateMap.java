/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class PsUpdateMap.
 */
public class PsUpdateMap extends OperationMap<String, RassEntity> {

	private Map<String , RassEntity> newValues;
	private Map<String, RassEntity> oldValues;

	/**
	 * Instantiates a new ps update map.
	 */
	public PsUpdateMap() {
		super();
	}

	/**
	 * Instantiates a new ps update map.
	 *
	 * @param operation the operation
	 */
	public PsUpdateMap(OperationType operation) {
		super(operation);

	}

	@Override
	public OperationType getOperation() {
		return OperationType.UPDATE;
	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
	}
}
