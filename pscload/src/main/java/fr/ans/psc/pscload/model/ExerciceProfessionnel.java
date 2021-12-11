package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fr.ans.psc.model.Profession;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

}
