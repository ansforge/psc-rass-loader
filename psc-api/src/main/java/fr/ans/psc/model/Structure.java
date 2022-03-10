/*
 * psc-api-maj-v2
 * API CRUD for Personnels et Structures de santé
 *
 * OpenAPI spec version: 1.0
 * Contact: superviseurs.psc@esante.gouv.fr
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package fr.ans.psc.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import javax.validation.constraints.*;
import javax.validation.Valid;
/**
 * Structure de Santé
 */
@Schema(description = "Structure de Santé")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-12-30T08:44:48.497Z[GMT]")
public class Structure implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("siteSIRET")
  private String siteSIRET = null;

  @JsonProperty("siteSIREN")
  private String siteSIREN = null;

  @JsonProperty("siteFINESS")
  private String siteFINESS = null;

  @JsonProperty("legalEstablishmentFINESS")
  private String legalEstablishmentFINESS = null;

  @JsonProperty("structureTechnicalId")
  private String structureTechnicalId = null;

  @JsonProperty("legalCommercialName")
  private String legalCommercialName = null;

  @JsonProperty("publicCommercialName")
  private String publicCommercialName = null;

  @JsonProperty("recipientAdditionalInfo")
  private String recipientAdditionalInfo = null;

  @JsonProperty("geoLocationAdditionalInfo")
  private String geoLocationAdditionalInfo = null;

  @JsonProperty("streetNumber")
  private String streetNumber = null;

  @JsonProperty("streetNumberRepetitionIndex")
  private String streetNumberRepetitionIndex = null;

  @JsonProperty("streetCategoryCode")
  private String streetCategoryCode = null;

  @JsonProperty("streetLabel")
  private String streetLabel = null;

  @JsonProperty("distributionMention")
  private String distributionMention = null;

  @JsonProperty("cedexOffice")
  private String cedexOffice = null;

  @JsonProperty("postalCode")
  private String postalCode = null;

  @JsonProperty("communeCode")
  private String communeCode = null;

  @JsonProperty("countryCode")
  private String countryCode = null;

  @JsonProperty("phone")
  private String phone = null;

  @JsonProperty("phone2")
  private String phone2 = null;

  @JsonProperty("fax")
  private String fax = null;

  @JsonProperty("email")
  private String email = null;

  @JsonProperty("departmentCode")
  private String departmentCode = null;

  @JsonProperty("oldStructureId")
  private String oldStructureId = null;

  public Structure siteSIRET(String siteSIRET) {
    this.siteSIRET = siteSIRET;
    return this;
  }

   /**
   * Get siteSIRET
   * @return siteSIRET
  **/
  @Schema(description = "")
  public String getSiteSIRET() {
    return siteSIRET;
  }

  public void setSiteSIRET(String siteSIRET) {
    this.siteSIRET = siteSIRET;
  }

  public Structure siteSIREN(String siteSIREN) {
    this.siteSIREN = siteSIREN;
    return this;
  }

   /**
   * Get siteSIREN
   * @return siteSIREN
  **/
  @Schema(description = "")
  public String getSiteSIREN() {
    return siteSIREN;
  }

  public void setSiteSIREN(String siteSIREN) {
    this.siteSIREN = siteSIREN;
  }

  public Structure siteFINESS(String siteFINESS) {
    this.siteFINESS = siteFINESS;
    return this;
  }

   /**
   * Get siteFINESS
   * @return siteFINESS
  **/
  @Schema(description = "")
  public String getSiteFINESS() {
    return siteFINESS;
  }

  public void setSiteFINESS(String siteFINESS) {
    this.siteFINESS = siteFINESS;
  }

  public Structure legalEstablishmentFINESS(String legalEstablishmentFINESS) {
    this.legalEstablishmentFINESS = legalEstablishmentFINESS;
    return this;
  }

   /**
   * Get legalEstablishmentFINESS
   * @return legalEstablishmentFINESS
  **/
  @Schema(description = "")
  public String getLegalEstablishmentFINESS() {
    return legalEstablishmentFINESS;
  }

  public void setLegalEstablishmentFINESS(String legalEstablishmentFINESS) {
    this.legalEstablishmentFINESS = legalEstablishmentFINESS;
  }

  public Structure structureTechnicalId(String structureTechnicalId) {
    this.structureTechnicalId = structureTechnicalId;
    return this;
  }

   /**
   * Get structureTechnicalId
   * @return structureTechnicalId
  **/
  @NotNull
 @Size(min=1)  @Schema(required = true, description = "")
  public String getStructureTechnicalId() {
    return structureTechnicalId;
  }

  public void setStructureTechnicalId(String structureTechnicalId) {
    this.structureTechnicalId = structureTechnicalId;
  }

  public Structure legalCommercialName(String legalCommercialName) {
    this.legalCommercialName = legalCommercialName;
    return this;
  }

   /**
   * Get legalCommercialName
   * @return legalCommercialName
  **/
  @Schema(description = "")
  public String getLegalCommercialName() {
    return legalCommercialName;
  }

  public void setLegalCommercialName(String legalCommercialName) {
    this.legalCommercialName = legalCommercialName;
  }

  public Structure publicCommercialName(String publicCommercialName) {
    this.publicCommercialName = publicCommercialName;
    return this;
  }

   /**
   * Get publicCommercialName
   * @return publicCommercialName
  **/
  @Schema(description = "")
  public String getPublicCommercialName() {
    return publicCommercialName;
  }

  public void setPublicCommercialName(String publicCommercialName) {
    this.publicCommercialName = publicCommercialName;
  }

  public Structure recipientAdditionalInfo(String recipientAdditionalInfo) {
    this.recipientAdditionalInfo = recipientAdditionalInfo;
    return this;
  }

   /**
   * Get recipientAdditionalInfo
   * @return recipientAdditionalInfo
  **/
  @Schema(description = "")
  public String getRecipientAdditionalInfo() {
    return recipientAdditionalInfo;
  }

  public void setRecipientAdditionalInfo(String recipientAdditionalInfo) {
    this.recipientAdditionalInfo = recipientAdditionalInfo;
  }

  public Structure geoLocationAdditionalInfo(String geoLocationAdditionalInfo) {
    this.geoLocationAdditionalInfo = geoLocationAdditionalInfo;
    return this;
  }

   /**
   * Get geoLocationAdditionalInfo
   * @return geoLocationAdditionalInfo
  **/
  @Schema(description = "")
  public String getGeoLocationAdditionalInfo() {
    return geoLocationAdditionalInfo;
  }

  public void setGeoLocationAdditionalInfo(String geoLocationAdditionalInfo) {
    this.geoLocationAdditionalInfo = geoLocationAdditionalInfo;
  }

  public Structure streetNumber(String streetNumber) {
    this.streetNumber = streetNumber;
    return this;
  }

   /**
   * Get streetNumber
   * @return streetNumber
  **/
  @Schema(description = "")
  public String getStreetNumber() {
    return streetNumber;
  }

  public void setStreetNumber(String streetNumber) {
    this.streetNumber = streetNumber;
  }

  public Structure streetNumberRepetitionIndex(String streetNumberRepetitionIndex) {
    this.streetNumberRepetitionIndex = streetNumberRepetitionIndex;
    return this;
  }

   /**
   * Get streetNumberRepetitionIndex
   * @return streetNumberRepetitionIndex
  **/
  @Schema(description = "")
  public String getStreetNumberRepetitionIndex() {
    return streetNumberRepetitionIndex;
  }

  public void setStreetNumberRepetitionIndex(String streetNumberRepetitionIndex) {
    this.streetNumberRepetitionIndex = streetNumberRepetitionIndex;
  }

  public Structure streetCategoryCode(String streetCategoryCode) {
    this.streetCategoryCode = streetCategoryCode;
    return this;
  }

   /**
   * Get streetCategoryCode
   * @return streetCategoryCode
  **/
  @Schema(description = "")
  public String getStreetCategoryCode() {
    return streetCategoryCode;
  }

  public void setStreetCategoryCode(String streetCategoryCode) {
    this.streetCategoryCode = streetCategoryCode;
  }

  public Structure streetLabel(String streetLabel) {
    this.streetLabel = streetLabel;
    return this;
  }

   /**
   * Get streetLabel
   * @return streetLabel
  **/
  @Schema(description = "")
  public String getStreetLabel() {
    return streetLabel;
  }

  public void setStreetLabel(String streetLabel) {
    this.streetLabel = streetLabel;
  }

  public Structure distributionMention(String distributionMention) {
    this.distributionMention = distributionMention;
    return this;
  }

   /**
   * Get distributionMention
   * @return distributionMention
  **/
  @Schema(description = "")
  public String getDistributionMention() {
    return distributionMention;
  }

  public void setDistributionMention(String distributionMention) {
    this.distributionMention = distributionMention;
  }

  public Structure cedexOffice(String cedexOffice) {
    this.cedexOffice = cedexOffice;
    return this;
  }

   /**
   * Get cedexOffice
   * @return cedexOffice
  **/
  @Schema(description = "")
  public String getCedexOffice() {
    return cedexOffice;
  }

  public void setCedexOffice(String cedexOffice) {
    this.cedexOffice = cedexOffice;
  }

  public Structure postalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

   /**
   * Get postalCode
   * @return postalCode
  **/
  @Schema(description = "")
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public Structure communeCode(String communeCode) {
    this.communeCode = communeCode;
    return this;
  }

   /**
   * Get communeCode
   * @return communeCode
  **/
  @Schema(description = "")
  public String getCommuneCode() {
    return communeCode;
  }

  public void setCommuneCode(String communeCode) {
    this.communeCode = communeCode;
  }

  public Structure countryCode(String countryCode) {
    this.countryCode = countryCode;
    return this;
  }

   /**
   * Get countryCode
   * @return countryCode
  **/
  @Schema(description = "")
  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public Structure phone(String phone) {
    this.phone = phone;
    return this;
  }

   /**
   * Get phone
   * @return phone
  **/
  @Schema(description = "")
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Structure phone2(String phone2) {
    this.phone2 = phone2;
    return this;
  }

   /**
   * Get phone2
   * @return phone2
  **/
  @Schema(description = "")
  public String getPhone2() {
    return phone2;
  }

  public void setPhone2(String phone2) {
    this.phone2 = phone2;
  }

  public Structure fax(String fax) {
    this.fax = fax;
    return this;
  }

   /**
   * Get fax
   * @return fax
  **/
  @Schema(description = "")
  public String getFax() {
    return fax;
  }

  public void setFax(String fax) {
    this.fax = fax;
  }

  public Structure email(String email) {
    this.email = email;
    return this;
  }

   /**
   * Get email
   * @return email
  **/
  @Schema(description = "")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Structure departmentCode(String departmentCode) {
    this.departmentCode = departmentCode;
    return this;
  }

   /**
   * Get departmentCode
   * @return departmentCode
  **/
  @Schema(description = "")
  public String getDepartmentCode() {
    return departmentCode;
  }

  public void setDepartmentCode(String departmentCode) {
    this.departmentCode = departmentCode;
  }

  public Structure oldStructureId(String oldStructureId) {
    this.oldStructureId = oldStructureId;
    return this;
  }

   /**
   * Get oldStructureId
   * @return oldStructureId
  **/
  @Schema(description = "")
  public String getOldStructureId() {
    return oldStructureId;
  }

  public void setOldStructureId(String oldStructureId) {
    this.oldStructureId = oldStructureId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Structure structure = (Structure) o;
    return Objects.equals(this.siteSIRET, structure.siteSIRET) &&
        Objects.equals(this.siteSIREN, structure.siteSIREN) &&
        Objects.equals(this.siteFINESS, structure.siteFINESS) &&
        Objects.equals(this.legalEstablishmentFINESS, structure.legalEstablishmentFINESS) &&
        Objects.equals(this.structureTechnicalId, structure.structureTechnicalId) &&
        Objects.equals(this.legalCommercialName, structure.legalCommercialName) &&
        Objects.equals(this.publicCommercialName, structure.publicCommercialName) &&
        Objects.equals(this.recipientAdditionalInfo, structure.recipientAdditionalInfo) &&
        Objects.equals(this.geoLocationAdditionalInfo, structure.geoLocationAdditionalInfo) &&
        Objects.equals(this.streetNumber, structure.streetNumber) &&
        Objects.equals(this.streetNumberRepetitionIndex, structure.streetNumberRepetitionIndex) &&
        Objects.equals(this.streetCategoryCode, structure.streetCategoryCode) &&
        Objects.equals(this.streetLabel, structure.streetLabel) &&
        Objects.equals(this.distributionMention, structure.distributionMention) &&
        Objects.equals(this.cedexOffice, structure.cedexOffice) &&
        Objects.equals(this.postalCode, structure.postalCode) &&
        Objects.equals(this.communeCode, structure.communeCode) &&
        Objects.equals(this.countryCode, structure.countryCode) &&
        Objects.equals(this.phone, structure.phone) &&
        Objects.equals(this.phone2, structure.phone2) &&
        Objects.equals(this.fax, structure.fax) &&
        Objects.equals(this.email, structure.email) &&
        Objects.equals(this.departmentCode, structure.departmentCode) &&
        Objects.equals(this.oldStructureId, structure.oldStructureId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(siteSIRET, siteSIREN, siteFINESS, legalEstablishmentFINESS, structureTechnicalId, legalCommercialName, publicCommercialName, recipientAdditionalInfo, geoLocationAdditionalInfo, streetNumber, streetNumberRepetitionIndex, streetCategoryCode, streetLabel, distributionMention, cedexOffice, postalCode, communeCode, countryCode, phone, phone2, fax, email, departmentCode, oldStructureId);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Structure {\n");
    
    sb.append("    siteSIRET: ").append(toIndentedString(siteSIRET)).append("\n");
    sb.append("    siteSIREN: ").append(toIndentedString(siteSIREN)).append("\n");
    sb.append("    siteFINESS: ").append(toIndentedString(siteFINESS)).append("\n");
    sb.append("    legalEstablishmentFINESS: ").append(toIndentedString(legalEstablishmentFINESS)).append("\n");
    sb.append("    structureTechnicalId: ").append(toIndentedString(structureTechnicalId)).append("\n");
    sb.append("    legalCommercialName: ").append(toIndentedString(legalCommercialName)).append("\n");
    sb.append("    publicCommercialName: ").append(toIndentedString(publicCommercialName)).append("\n");
    sb.append("    recipientAdditionalInfo: ").append(toIndentedString(recipientAdditionalInfo)).append("\n");
    sb.append("    geoLocationAdditionalInfo: ").append(toIndentedString(geoLocationAdditionalInfo)).append("\n");
    sb.append("    streetNumber: ").append(toIndentedString(streetNumber)).append("\n");
    sb.append("    streetNumberRepetitionIndex: ").append(toIndentedString(streetNumberRepetitionIndex)).append("\n");
    sb.append("    streetCategoryCode: ").append(toIndentedString(streetCategoryCode)).append("\n");
    sb.append("    streetLabel: ").append(toIndentedString(streetLabel)).append("\n");
    sb.append("    distributionMention: ").append(toIndentedString(distributionMention)).append("\n");
    sb.append("    cedexOffice: ").append(toIndentedString(cedexOffice)).append("\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
    sb.append("    communeCode: ").append(toIndentedString(communeCode)).append("\n");
    sb.append("    countryCode: ").append(toIndentedString(countryCode)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    phone2: ").append(toIndentedString(phone2)).append("\n");
    sb.append("    fax: ").append(toIndentedString(fax)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    departmentCode: ").append(toIndentedString(departmentCode)).append("\n");
    sb.append("    oldStructureId: ").append(toIndentedString(oldStructureId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
