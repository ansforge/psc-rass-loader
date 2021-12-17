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
	 * returnStatus after failure in change request
	 */
	private int returnStatus;

	
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
		setSiteSIRET(items[RassItems.SITE_SIRET.column]);
		setSiteSIREN(items[RassItems.SITE_SIREN.column]);
		setSiteFINESS(items[RassItems.SITE_FINESS.column]);
		setLegalEstablishmentFINESS(items[RassItems.LEGAL_ESTABLISHMENT_FINESS.column]);
		setStructureTechnicalId(items[RassItems.STRUCTURE_TECHNICAL_ID.column]);
		setLegalCommercialName(items[RassItems.LEGAL_COMMERCIAL_NAME.column]);
		setPublicCommercialName(items[RassItems.PUBLIC_COMMERCIAL_NAME.column]);
		setRecipientAdditionalInfo(items[RassItems.RECIPIENT_ADDITIONAL_INFO.column]);
		setGeoLocationAdditionalInfo(items[RassItems.GEO_LOCATION_ADDITIONAL_INFO.column]);
		setStreetNumber(items[RassItems.STREET_NUMBER.column]);
		setStreetNumberRepetitionIndex(items[RassItems.STREET_NUMBER_REPETITION_INDEX.column]);
		setStreetCategoryCode(items[RassItems.STREET_CATEGORY_CODE.column]);
		setStreetLabel(items[RassItems.STREET_LABEL.column]);
		setDistributionMention(items[RassItems.DISTRIBUTION_MENTION.column]);
		setCedexOffice(items[RassItems.CEDEX_OFFICE.column]);
		setPostalCode(items[RassItems.POSTAL_CODE.column]);
		setCommuneCode(items[RassItems.COMMUNE_CODE.column]);
		setCountryCode(items[RassItems.COUNTRY_CODE.column]);
		setPhone(items[RassItems.STRUCTURE_PHONE.column]);
		setPhone2(items[RassItems.STRUCTURE_PHONE_2.column]);
		setFax(items[RassItems.STRUCTURE_FAX.column]);
		setEmail(items[RassItems.STRUCTURE_EMAIL.column]);
		setDepartmentCode(items[RassItems.DEPARTMENT_CODE.column]);
		setOldStructureId(items[RassItems.OLD_STRUCTURE_ID.column]);
		setRegistrationAuthority(items[RassItems.REGISTRATION_AUTHORITY.column]);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(returnStatus);
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
		returnStatus = in.readInt();
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

	public int getReturnStatus() {
		return returnStatus;
	}

	public void setReturnStatus(int returnStatus) {
		this.returnStatus = returnStatus;
	}

}
