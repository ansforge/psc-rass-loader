package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import fr.ans.psc.model.Expertise;
import fr.ans.psc.model.Profession;
import fr.ans.psc.model.WorkSituation;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ExerciceProfessionnel extends Profession implements Externalizable {

	private static final long serialVersionUID = 546016744459782913L;

	public ExerciceProfessionnel(String[] items) {
		super();
		setCode(items[13]);
		setCategoryCode(items[14]);
		setSalutationCode(items[15]);
		setLastName(items[16]);
		setFirstName(items[17]);
		addExpertisesItem(new SavoirFaire(items));
		addWorkSituationsItem(new SituationExercice(items));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(getCode());
		out.writeObject(getCategoryCode());
		out.writeObject(getSalutationCode());
		out.writeObject(getLastName());
		out.writeObject(getFirstName());
		out.writeObject(getExpertises());
		out.writeObject(getWorkSituations());

	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setCode((String) in.readObject());
		setCategoryCode((String) in.readObject());
		setSalutationCode((String) in.readObject());
		setLastName((String) in.readObject());
		setFirstName((String) in.readObject());
		setExpertises((List<Expertise>) in.readObject());
		setWorkSituations((List<WorkSituation>) in.readObject());

	}

}
