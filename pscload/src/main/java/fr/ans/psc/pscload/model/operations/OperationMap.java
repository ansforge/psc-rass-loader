/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import com.esotericsoftware.kryo.KryoSerializable;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.OperationType;
import fr.ans.psc.pscload.visitor.Visitable;

import java.util.Map;

/**
 * The Class OperationMap.
 *
 * @param <K> the key type
 * @param <V> the value type
 */

public interface OperationMap<K, V> extends Visitable, KryoSerializable {

	 OperationType getOperation();

	/**
	 * Save old value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	 void saveNewValue(String key, RassEntity value);

	/**
	 * Gets the old value.
	 *
	 * @param key the key
	 * @return the old value
	 */
	RassEntity getNewValue(String key);
	/**
	 * Save old value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	default void saveOldValue(String key, RassEntity value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the old value.
	 *
	 * @param key the key
	 * @return the old value
	 */
	default RassEntity getOldValue(String key) {
		throw new UnsupportedOperationException();
	}

	Map<String, RassEntity> getNewValues();

	default Map<String, RassEntity> getOldValues() {
		throw new UnsupportedOperationException();
	}
}
