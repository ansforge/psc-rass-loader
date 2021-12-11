package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fr.ans.psc.model.StructureRef;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class RefStructure extends StructureRef implements Externalizable {

	public RefStructure(String structureId) {
		super();
		setStructureId(structureId);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub

	}

}
