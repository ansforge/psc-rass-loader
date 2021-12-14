/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class Structure extends fr.ans.psc.model.Structure implements Externalizable {

	/**
	 * Instantiates a new structure.
	 */
	public Structure() {
		super();
	}

	/**
	 * Instantiates a new structure.
	 *
	 * @param items the items
	 */
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
		out.writeObject(getSiteSIRET());
		out.writeObject(getSiteSIREN());
		out.writeObject(getSiteFINESS());
		out.writeObject(getLegalEstablishmentFINESS());
		out.writeObject(getStructureTechnicalId());
		out.writeObject(getLegalCommercialName());
		out.writeObject(getPublicCommercialName());
		out.writeObject(getRecipientAdditionalInfo());
		out.writeObject(getGeoLocationAdditionalInfo());
		out.writeObject(getStreetNumber());
		out.writeObject(getStreetNumberRepetitionIndex());
		out.writeObject(getStreetCategoryCode());
		out.writeObject(getStreetLabel());
		out.writeObject(getDistributionMention());
		out.writeObject(getCedexOffice());
		out.writeObject(getPostalCode());
		out.writeObject(getCommuneCode());
		out.writeObject(getCountryCode());
		out.writeObject(getPhone());
		out.writeObject(getPhone2());
		out.writeObject(getFax());
		out.writeObject(getEmail());
		out.writeObject(getDepartmentCode());
		out.writeObject(getOldStructureId());
		out.writeObject(getRegistrationAuthority());

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setSiteSIRET((String) in.readObject());
		setSiteFINESS((String) in.readObject());
		setLegalEstablishmentFINESS((String) in.readObject());
		setStructureTechnicalId((String) in.readObject());
		setLegalCommercialName((String) in.readObject());
		setPublicCommercialName((String) in.readObject());
		setRecipientAdditionalInfo((String) in.readObject());
		setGeoLocationAdditionalInfo((String) in.readObject());
		setStreetNumber((String) in.readObject());
		setStreetNumberRepetitionIndex((String) in.readObject());
		setStreetCategoryCode((String) in.readObject());
		setStreetLabel((String) in.readObject());
		setDistributionMention((String) in.readObject());
		setCedexOffice((String) in.readObject());
		setPostalCode((String) in.readObject());
		setCommuneCode((String) in.readObject());
		setCountryCode((String) in.readObject());
		setPhone((String) in.readObject());
		setPhone2((String) in.readObject());
		setFax((String) in.readObject());
		setEmail((String) in.readObject());
		setDepartmentCode((String) in.readObject());
		setOldStructureId((String) in.readObject());
		setRegistrationAuthority((String) in.readObject());
	}

}
