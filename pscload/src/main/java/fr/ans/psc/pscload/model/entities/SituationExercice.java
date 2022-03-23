/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.entities;

import fr.ans.psc.model.WorkSituation;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class SituationExercice extends WorkSituation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2664005257511101075L;

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
		setRegistrationAuthority(items[RassItems.REGISTRATION_AUTHORITY.column]);
		if (!items[RassItems.STRUCTURE_TECHNICAL_ID.column].isBlank()) {
			setStructure(new Structure(items));
//			addStructuresItem(new RefStructure(items[RassItems.STRUCTURE_TECHNICAL_ID.column])); // structureTechnicalId
		}
	}

	public void setSituationExerciceItems(String[] items) {
		items[RassItems.SITUATION_MODE_CODE.column] = getModeCode();
		items[RassItems.ACTIVITY_SECTOR_CODE.column] = getActivitySectorCode();
		items[RassItems.PHARMACIST_TABLE_SECTION_CODE.column] = getPharmacistTableSectionCode();
		items[RassItems.SITUATION_ROLE_CODE.column] = getRoleCode();
		items[RassItems.REGISTRATION_AUTHORITY.column] = getRegistrationAuthority();
	}
}
