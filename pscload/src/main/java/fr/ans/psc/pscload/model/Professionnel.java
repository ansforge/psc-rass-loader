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

@EqualsAndHashCode(callSuper = true)
public class Professionnel extends Ps implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2859304844064466893L;

	public Professionnel(String[] items, boolean deep) {
		super();
		setIdType(items[0]);
		setId(items[1]);
		setNationalId(items[2]);
		setLastName(items[3]);
		setFirstName(items[4]);
		setDateOfBirth(items[5]);
		setBirthAddressCode(items[6]);
		setBirthCountryCode(items[7]);
		setBirthAddress(items[8]);
		setGenderCode(items[9]);
		setPhone(items[10]);
		setEmail(items[11]);
		setSalutationCode(items[12]);
		if (deep) {
			addProfessionsItem(new ExerciceProfessionnel(items));
		}
	}

	public Optional<Profession> getProfessionByCodeAndCategory(String code, String category) {
		return getProfessions().stream()
				.filter(exo -> exo.getCode().concat(exo.getCategoryCode()).equals(code.concat(category))).findAny();

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
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

}
