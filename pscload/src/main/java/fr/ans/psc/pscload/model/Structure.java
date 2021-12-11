package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class Structure extends fr.ans.psc.model.Structure implements Externalizable {

	public Structure(String[] items) {
		super();
		setSiteSIRET(items[24]);
		setSiteSIREN(items[25]);
		setSiteFINESS(items[26]);
		setLegalEstablishmentFINESS(items[27]);
		setStructureTechnicalId(items[28]);
		setLegalCommercialName(items[29]);
		setPublicCommercialName(items[30]);
		setRecipientAdditionalInfo(items[31]);
		setGeoLocationAdditionalInfo(items[32]);
		setStreetNumber(items[33]);
		setStreetNumberRepetitionIndex(items[34]);
		setStreetCategoryCode(items[35]);
		setStreetLabel(items[36]);
		setDistributionMention(items[37]);
		setCedexOffice(items[38]);
		setPostalCode(items[39]);
		setCommuneCode(items[40]);
		setCountryCode(items[41]);
		setPhone(items[42]);
		setPhone2(items[43]);
		setFax(items[44]);
		setEmail(items[45]);
		setDepartmentCode(items[46]);
		setOldStructureId(items[47]);
		setRegistrationAuthority(items[48]);
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
