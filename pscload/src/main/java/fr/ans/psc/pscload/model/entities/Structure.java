/**
 * Copyright (C) 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscload.model.entities;

import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class Structure extends fr.ans.psc.model.Structure implements RassEntity {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4792642166799662339L;
	
	/**
	 * returnStatus after failure in change request
	 */
	private int returnStatus = 100;

	
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
	}

	public Structure(fr.ans.psc.model.Structure structure) {
		super();
		setSiteSIRET(structure.getSiteSIRET());
		setSiteSIREN(structure.getSiteSIREN());
		setSiteFINESS(structure.getSiteFINESS());
		setLegalEstablishmentFINESS(structure.getLegalEstablishmentFINESS());
		setStructureTechnicalId(structure.getStructureTechnicalId());
		setLegalCommercialName(structure.getLegalCommercialName());
		setPublicCommercialName(structure.getPublicCommercialName());
		setRecipientAdditionalInfo(structure.getRecipientAdditionalInfo());
		setGeoLocationAdditionalInfo(structure.getGeoLocationAdditionalInfo());
		setStreetNumber(structure.getStreetNumber());
		setStreetNumberRepetitionIndex(structure.getStreetNumberRepetitionIndex());
		setStreetCategoryCode(structure.getStreetCategoryCode());
		setStreetLabel(structure.getStreetLabel());
		setDistributionMention(structure.getDistributionMention());
		setCedexOffice(structure.getCedexOffice());
		setPostalCode(structure.getPostalCode());
		setCommuneCode(structure.getCommuneCode());
		setCountryCode(structure.getCountryCode());
		setPhone(structure.getPhone());
		setPhone2(structure.getPhone2());
		setFax(structure.getFax());
		setEmail(structure.getEmail());
		setDepartmentCode(structure.getDepartmentCode());
		setOldStructureId(structure.getOldStructureId());
	}

	public void setStructureItems(String[] items) {
		items[RassItems.SITE_SIRET.column] = getSiteSIRET();
		items[RassItems.SITE_SIREN.column] = getSiteSIREN();
		items[RassItems.SITE_FINESS.column] = getSiteFINESS();
		items[RassItems.LEGAL_ESTABLISHMENT_FINESS.column] = getLegalEstablishmentFINESS();
		items[RassItems.STRUCTURE_TECHNICAL_ID.column] = getStructureTechnicalId();
		items[RassItems.LEGAL_COMMERCIAL_NAME.column] = getLegalCommercialName();
		items[RassItems.PUBLIC_COMMERCIAL_NAME.column] = getPublicCommercialName();
		items[RassItems.RECIPIENT_ADDITIONAL_INFO.column] = getRecipientAdditionalInfo();
		items[RassItems.GEO_LOCATION_ADDITIONAL_INFO.column] = getGeoLocationAdditionalInfo();
		items[RassItems.STREET_NUMBER.column] = getStreetNumber();
		items[RassItems.STREET_NUMBER_REPETITION_INDEX.column] = getStreetNumberRepetitionIndex();
		items[RassItems.STREET_CATEGORY_CODE.column] = getStreetCategoryCode();
		items[RassItems.STREET_LABEL.column] = getStreetLabel();
		items[RassItems.DISTRIBUTION_MENTION.column] = getDistributionMention();
		items[RassItems.CEDEX_OFFICE.column] = getCedexOffice();
		items[RassItems.POSTAL_CODE.column] = getPostalCode();
		items[RassItems.COMMUNE_CODE.column] = getCommuneCode();
		items[RassItems.COUNTRY_CODE.column] = getCountryCode();
		items[RassItems.STRUCTURE_PHONE.column] = getPhone();
		items[RassItems.STRUCTURE_PHONE_2.column] = getPhone2();
		items[RassItems.STRUCTURE_FAX.column] = getFax();
		items[RassItems.STRUCTURE_EMAIL.column] = getEmail();
		items[RassItems.DEPARTMENT_CODE.column] = getDepartmentCode();
		items[RassItems.OLD_STRUCTURE_ID.column] = getOldStructureId();
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
		return getStructureTechnicalId();
	}

	@Override
	public String getIdType() {
		return "ALL";
	}

}
