/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.entities;

import java.util.ArrayList;
import java.util.List;

import fr.ans.psc.model.Expertise;
import fr.ans.psc.model.Profession;
import fr.ans.psc.model.WorkSituation;
import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */

/**
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

	public void setExerciceProfessionnelItems(String[] items) {
		items[RassItems.EX_PRO_CODE.column] = getCode();
		items[RassItems.CATEGORY_CODE.column] = getCategoryCode();
		items[RassItems.EX_PRO_SALUTATION_CODE.column] = getSalutationCode();
		items[RassItems.EX_PRO_LAST_NAME.column] = getLastName();
		items[RassItems.EX_PRO_FIRST_NAME.column] = getFirstName();
	}

	public List<SavoirFaire> getSavoirFaire() {
		List<Expertise> expertises = getExpertises();
		List<SavoirFaire> savoirFaireList = new ArrayList<>();
		expertises.forEach(expertise -> savoirFaireList.add((SavoirFaire) expertise));
		return savoirFaireList;
	}

	public List<SituationExercice> getSituationsExercice() {
		List<WorkSituation> workSituations = getWorkSituations();
		List<SituationExercice> situationsExercice = new ArrayList<>();
		workSituations.forEach(workSituation -> situationsExercice.add((SituationExercice) workSituation));
		return situationsExercice;
	}

}
