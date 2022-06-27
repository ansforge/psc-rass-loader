/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.entities;

import fr.ans.psc.model.Profession;
import fr.ans.psc.model.Ps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Professionnel extends Ps implements RassEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2859304844064466893L;
	
	/**
	 * returnStatus after failure in change request
	 */
	private int returnStatus = 100;

	/**
	 * Instantiates a new professionnel.
	 */
	public Professionnel() {
		super();
	}

	/**
	 * Instantiates a new professionnel.
	 *
	 * @param items the items
	 * @param deep the deep
	 */
	public Professionnel(String[] items, boolean deep) {
		super();
		setIdType( items[RassItems.ID_TYPE.column]);
		setId( items[RassItems.ID.column]);
		setNationalId( items[RassItems.NATIONAL_ID.column]);
		setLastName( items[RassItems.LAST_NAME.column]);
		setFirstName( new ArrayList<String> (Arrays.asList(items[RassItems.FIRST_NAME.column].split("'"))));
		setDateOfBirth( items[RassItems.DOB.column]);
		setBirthAddressCode( items[RassItems.BIRTH_ADDRESS_CODE.column]);
		setBirthCountryCode( items[RassItems.BIRTH_COUNTRY_CODE.column]);
		setBirthAddress( items[RassItems.BIRTH_ADDRESS.column]);
		setGenderCode( items[RassItems.GENDER_CODE.column]);
		setPhone( items[RassItems.PHONE.column]);
		setEmail( items[RassItems.EMAIL.column]);
		setSalutationCode( items[RassItems.SALUTATION_CODE.column]);
		if (deep) {
			addProfessionsItem(new ExerciceProfessionnel(items));
		}
	}

	public void setProfessionnelItems(String[] items) {
		items[RassItems.ID_TYPE.column] = getIdType();
		items[RassItems.ID.column] = getId();
		items[RassItems.NATIONAL_ID.column] = getNationalId();
		items[RassItems.LAST_NAME.column] = getLastName();
		items[RassItems.FIRST_NAME.column] = getFirstName().toString().replace("[","").replace("]","").replace(",","'").replace(" ","");
		items[RassItems.DOB.column] = getDateOfBirth();
		items[RassItems.BIRTH_ADDRESS_CODE.column] = getBirthAddressCode();
		items[RassItems.BIRTH_COUNTRY_CODE.column] = getBirthCountryCode();
		items[RassItems.BIRTH_ADDRESS.column] = getBirthAddress();
		items[RassItems.GENDER_CODE.column] = getGenderCode();
		items[RassItems.PHONE.column] = getPhone();
		items[RassItems.EMAIL.column] = getEmail();
		items[RassItems.SALUTATION_CODE.column] = getSalutationCode();
		items[RassItems.IDS.column] = getIds() == null ? "" : getIds().toString();
		items[RassItems.ACTIVATED.column] = getActivated() == null ? "" : getActivated().toString();
		items[RassItems.DEACTIVATED.column] = getDeactivated() == null ? "" : getDeactivated().toString();
	}

	/**
	 * Gets the profession by code and category.
	 *
	 * @param code the code
	 * @param category the category
	 * @return the profession by code and category
	 */
	public Optional<Profession> getProfessionByCodeAndCategory(String code, String category) {
		return getProfessions().stream()
				.filter(exo -> exo.getCode().concat(exo.getCategoryCode()).equals(code.concat(category))).findAny();

	}

	@Override
	public int getReturnStatus() {
		return returnStatus;
	}

	@Override
	public void setReturnStatus(int returnStatus) {
		this.returnStatus = returnStatus;
	}

	@Override
	public String getInternalId() {
		return getNationalId();
	}
	
	@Override
	public String getIdType() {
		return super.getIdType();
	}

	public List<ExerciceProfessionnel> getExercicesProfessionels() {
		List<Profession> professions = getProfessions();
		List<ExerciceProfessionnel> exercicesProfessionnels = new ArrayList<>();
		professions.forEach(profession -> exercicesProfessionnels.add((ExerciceProfessionnel) profession));
		return exercicesProfessionnels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Professionnel professionnel = (Professionnel) o;

		return Objects.equals(this.getIdType(), professionnel.getIdType()) &&
				Objects.equals(this.getId(), professionnel.getId()) &&
				Objects.equals(this.getNationalId(), professionnel.getNationalId()) &&
				Objects.equals(this.getLastName(), professionnel.getLastName()) &&
				this.getFirstName().containsAll(professionnel.getFirstName()) &&
				Objects.equals(this.getDateOfBirth(), professionnel.getDateOfBirth()) &&
				Objects.equals(this.getBirthAddressCode(), professionnel.getBirthAddressCode()) &&
				Objects.equals(this.getBirthCountryCode(), professionnel.getBirthCountryCode()) &&
				Objects.equals(this.getBirthAddress(), professionnel.getBirthAddress()) &&
				Objects.equals(this.getGenderCode(), professionnel.getGenderCode()) &&
				Objects.equals(this.getPhone(), professionnel.getPhone()) &&
				Objects.equals(this.getEmail(), professionnel.getEmail()) &&
				Objects.equals(this.getSalutationCode(), professionnel.getSalutationCode()) &&
				Objects.equals(this.getExercicesProfessionels().size(), professionnel.getExercicesProfessionels().size()) &&
				this.getExercicesProfessionels().containsAll(professionnel.getExercicesProfessionels()) &&
				(this.getIds()==null || this.getIds().containsAll(professionnel.getIds())) &&
				Objects.equals(this.getActivated(), professionnel.getActivated()) &&
				Objects.equals(this.getDeactivated(), professionnel.getDeactivated());
	}

//	we have to reduce all list hash codes to ensure unsorted lists always return the same hash code
	@Override
	public int hashCode() {
		return Objects.hash(getIdType(), getId(), getNationalId(), getLastName(), getFirstName(),
				getDateOfBirth(), getBirthAddressCode(), getBirthCountryCode(), getBirthAddress(),
				getGenderCode(), getPhone(), getEmail(), getSalutationCode(),
				getExercicesProfessionels().stream().map(ExerciceProfessionnel::hashCode).reduce(0, Integer::sum),
				getIds(), getActivated(), getDeactivated());
	}
}
