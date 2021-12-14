/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fr.ans.psc.model.StructureRef;
import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class RefStructure extends StructureRef implements Externalizable {

	/**
	 * Instantiates a new ref structure.
	 */
	public RefStructure() {
		super();
	}

	/**
	 * Instantiates a new ref structure.
	 *
	 * @param structureId the structure id
	 */
	public RefStructure(String structureId) {
		super();
		setStructureId(structureId);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(getStructureId());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setStructureId((String) in.readObject());
	}

}
