/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import fr.ans.psc.model.StructureRef;
import fr.ans.psc.model.WorkSituation;
import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class SituationExercice extends WorkSituation implements Externalizable {

	/**
	 * Instantiates a new situation exercice.
	 */
	public SituationExercice() {
		super();
	}

	/**
	 * Instantiates a new situation exercice.
	 *
	 * @param items the items
	 */
	public SituationExercice(String[] items) {
		super();
		setModeCode(items[RassItems.SITUATION_MODE_CODE.column]);
		setActivitySectorCode(items[RassItems.ACTIVITY_SECTOR_CODE.column]);
		setPharmacistTableSectionCode(items[RassItems.PHARMACIST_TABLE_SECTION_CODE.column]);
		setRoleCode(items[RassItems.SITUATION_ROLE_CODE.column]);
		addStructuresItem(new RefStructure(items[28])); // structureTechnicalId
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(getModeCode());
		out.writeObject(getActivitySectorCode());
		out.writeObject(getPharmacistTableSectionCode());
		out.writeObject(getRoleCode());
		out.writeObject(getStructures());
		

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
