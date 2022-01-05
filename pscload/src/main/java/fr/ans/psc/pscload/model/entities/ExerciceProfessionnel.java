/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.entities;

import fr.ans.psc.model.Profession;
import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class ExerciceProfessionnel extends Profession {

	private static final long serialVersionUID = 546016744459782913L;

	
	/**
	 * Instantiates a new exercice professionnel.
	 */
	public ExerciceProfessionnel() {
		super();
	}

	/**
	 * Instantiates a new exercice professionnel.
	 *
	 * @param items the items
	 */
	public ExerciceProfessionnel(String[] items) {
		super();
		setCode(items[RassItems.EX_PRO_CODE.column]);
		setCategoryCode(items[RassItems.CATEGORY_CODE.column]);
		setSalutationCode(items[RassItems.EX_PRO_SALUTATION_CODE.column]);
		setLastName(items[RassItems.EX_PRO_LAST_NAME.column]);
		setFirstName(items[RassItems.EX_PRO_FIRST_NAME.column]);
		addExpertisesItem(new SavoirFaire(items));
		addWorkSituationsItem(new SituationExercice(items));
	}

}
