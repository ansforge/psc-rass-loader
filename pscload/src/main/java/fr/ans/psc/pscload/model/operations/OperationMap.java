/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.OperationType;
import fr.ans.psc.pscload.visitor.Visitable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The Class OperationMap.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@Getter
@Setter
public abstract class OperationMap<K, V> implements Visitable
//		, KryoSerializable
{

	private ConcurrentHashMap<String , RassEntity> newValues;
	private ConcurrentHashMap<String, RassEntity> oldValues;

	
	/**
	 * Instantiates a new operation map.
	 */
	public OperationMap() { }

	public abstract OperationType getOperation();

	/**
	 * Save old value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void saveNewValue(String key, RassEntity value) {
		if (newValues == null) {
			newValues = new ConcurrentHashMap<>();
		}
		newValues.put(key, value);
	}

	/**
	 * Gets the old value.
	 *
	 * @param key the key
	 * @return the old value
	 */
	public RassEntity getNewValue(String key) {
		if (newValues == null) {
			newValues = new ConcurrentHashMap<>();
		}
		return newValues.get(key);
	}
	/**
	 * Save old value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void saveOldValue(String key, RassEntity value) {
		if (oldValues == null) {
			oldValues = new ConcurrentHashMap<>();
		}
		oldValues.put(key, value);
	}

	/**
	 * Gets the old value.
	 *
	 * @param key the key
	 * @return the old value
	 */
	public RassEntity getOldValue(String key) {
		if (oldValues == null) {
			oldValues = new ConcurrentHashMap<>();
		}
		return oldValues.get(key);
	}

	public ConcurrentHashMap<String, RassEntity> getNewValues() {
		if (newValues == null) {
			newValues = new ConcurrentHashMap<>();
		}
		return newValues;
	}

//	@Override
//	public void write(Kryo kryo, Output output) {
//		kryo.writeObjectOrNull(output, oldValues, ConcurrentHashMap.class);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public void read(Kryo kryo, Input input) {
//		oldValues = (ConcurrentMap<String, RassEntity>) kryo.readObjectOrNull(input, ConcurrentHashMap.class);
//	}

}
