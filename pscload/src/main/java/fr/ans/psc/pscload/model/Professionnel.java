/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Optional;

import fr.ans.psc.model.Profession;
import fr.ans.psc.model.Ps;
import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class Professionnel extends Ps implements Externalizable {

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
		setIdType(items[RassItems.ID_TYPE.column]);
		setId(items[RassItems.ID.column]);
		setNationalId(items[RassItems.NATIONAL_ID.column]);
		setLastName(items[RassItems.LAST_NAME.column]);
		setFirstName(items[RassItems.FIRST_NAME.column]);
		setDateOfBirth(items[RassItems.DOB.column]);
		setBirthAddressCode(items[RassItems.BIRTH_ADDRESS_CODE.column]);
		setBirthCountryCode(items[RassItems.BIRTH_COUNTRY_CODE.column]);
		setBirthAddress(items[RassItems.BIRTH_ADDRESS.column]);
		setGenderCode(items[RassItems.GENDER_CODE.column]);
		setPhone(items[RassItems.PHONE.column]);
		setEmail(items[RassItems.EMAIL.column]);
		setSalutationCode(items[RassItems.SALUTATION_CODE.column]);
		if (deep) {
			addProfessionsItem(new ExerciceProfessionnel(items));
		}
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
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(returnStatus);
		out.writeObject(getIdType());
		out.writeObject(getId());
		out.writeObject(getNationalId());
		out.writeObject(getLastName());
		out.writeObject(getFirstName());
		out.writeObject(getDateOfBirth());
		out.writeObject(getBirthAddressCode());
		out.writeObject(getBirthCountryCode());
		out.writeObject(getBirthAddress());
		out.writeObject(getGenderCode());
		out.writeObject(getPhone());
		out.writeObject(getEmail());
		out.writeObject(getSalutationCode());
		out.writeObject(getProfessions());

		

	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		returnStatus = in.readInt();
		setIdType((String) in.readObject());
		setId((String) in.readObject());
		setNationalId((String) in.readObject());
		setLastName((String) in.readObject());
		setFirstName((String) in.readObject());
		setDateOfBirth((String) in.readObject());
		setBirthAddressCode((String) in.readObject());
		setBirthCountryCode((String) in.readObject());
		setBirthAddress((String) in.readObject());
		setGenderCode((String) in.readObject());
		setPhone((String) in.readObject());
		setEmail((String) in.readObject());
		setSalutationCode((String) in.readObject());
		setProfessions((List<Profession>) in.readObject());
	}

	public int getReturnStatus() {
		return returnStatus;
	}

	public void setReturnStatus(int returnStatus) {
		this.returnStatus = returnStatus;
	}

}
