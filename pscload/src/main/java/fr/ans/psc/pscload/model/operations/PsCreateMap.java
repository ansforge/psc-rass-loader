/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The Class PsCreateMap.
 */
public class PsCreateMap implements OperationMap<String, RassEntity> {

	private ConcurrentMap<String , RassEntity> newValues;

	/**
	 * Instantiates a new ps create map.
	 */
	public PsCreateMap() {
	}

	@Override
	public OperationType getOperation() {
		return OperationType.PS_CREATE;
	}

	@Override
	public void saveNewValue(String key, RassEntity value) {
		if (newValues == null) {
			newValues = new ConcurrentHashMap<>();
		}
		newValues.put(key, value);
	}

	@Override
	public RassEntity getNewValue(String key) {
		if (newValues == null) {
			newValues = new ConcurrentHashMap<>();
		}
		return newValues.get(key);
	}

	@Override
	public ConcurrentMap<String, RassEntity> getNewValues() {
		if (newValues == null) {
			newValues = new ConcurrentHashMap<>();
		}
		return newValues;
	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
	}

}
