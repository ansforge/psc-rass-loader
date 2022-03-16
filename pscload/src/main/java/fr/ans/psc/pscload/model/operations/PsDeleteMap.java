/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class PsDeleteMap.
 */
public class PsDeleteMap implements OperationMap<String, RassEntity> {

	private Map<String , RassEntity> newValues;

	/**
	 * Instantiates a new ps delete map.
	 */
	public PsDeleteMap() {
	}

	@Override
	public OperationType getOperation() {
		return OperationType.PS_DELETE;
	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
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
	public Map<String, RassEntity> getNewValues() {
		if (newValues == null) {
			newValues = new ConcurrentHashMap<>();
		}
		return newValues;
	}

}
