package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import fr.ans.psc.model.StructureRef;
import fr.ans.psc.model.WorkSituation;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class SituationExercice extends WorkSituation implements Externalizable {

	public SituationExercice(String[] items) {
		super();
		setModeCode(items[20]);
		setActivitySectorCode(items[21]);
		setPharmacistTableSectionCode(items[22]);
		setRoleCode(items[23]);
		addStructuresItem(new RefStructure(items[28])); // structureTechnicalId
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {


	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setModeCode((String) in.readObject());
		setActivitySectorCode((String) in.readObject());
		setPharmacistTableSectionCode((String) in.readObject());
		setRoleCode((String) in.readObject());
		setStructures((List<StructureRef>) in.readObject());

	}

}
