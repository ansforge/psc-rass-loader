/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class PsUpdateMap.
 */
public class PsUpdateMap implements OperationMap<String, RassEntity> {

	private ConcurrentHashMap<String , RassEntity> newValues;
	private ConcurrentHashMap<String, RassEntity> oldValues;

	/**
	 * Instantiates a new ps update map.
	 */
	public PsUpdateMap() {
	}

	@Override
	public OperationType getOperation() {
		return OperationType.PS_UPDATE;
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
	public ConcurrentHashMap<String, RassEntity> getNewValues() {
		if (newValues == null) {
			newValues = new ConcurrentHashMap<>();
		}
		return newValues;
	}

	@Override
	public void saveOldValue(String key, RassEntity value) {
		if (oldValues == null) {
			oldValues = new ConcurrentHashMap<>();
		}
		oldValues.put(key, value);
	}

	@Override
	public RassEntity getOldValue(String key) {
		if (oldValues == null) {
			oldValues = new ConcurrentHashMap<>();
		}
		return oldValues.get(key);
	}

	@Override
	public ConcurrentHashMap<String, RassEntity> getOldValues() {
		if (oldValues == null) {
			oldValues = new ConcurrentHashMap<>();
		}
		return oldValues;
	}


	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
	}
}
