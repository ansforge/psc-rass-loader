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
import fr.ans.psc.model.Expertise;
import fr.ans.psc.model.WorkSituation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import javax.validation.constraints.*;
import javax.validation.Valid;
/**
 * Profession
 */
@Schema(description = "Profession")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-12-30T08:44:48.497Z[GMT]")
public class Profession implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("exProId")
  private String exProId = null;

  @JsonProperty("code")
  private String code = null;

  @JsonProperty("categoryCode")
  private String categoryCode = null;

  @JsonProperty("salutationCode")
  private String salutationCode = null;

  @JsonProperty("lastName")
  private String lastName = null;

  @JsonProperty("firstName")
  private String firstName = null;

  @JsonProperty("expertises")
  private List<Expertise> expertises = null;

  @JsonProperty("workSituations")
  private List<WorkSituation> workSituations = null;

  public Profession exProId(String exProId) {
    this.exProId = exProId;
    return this;
  }

   /**
   * Get exProId
   * @return exProId
  **/
  @Schema(description = "")
  public String getExProId() {
    return exProId;
  }

  public void setExProId(String exProId) {
    this.exProId = exProId;
  }

  public Profession code(String code) {
    this.code = code;
    return this;
  }

   /**
   * Get code
   * @return code
  **/
  @Schema(description = "")
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Profession categoryCode(String categoryCode) {
    this.categoryCode = categoryCode;
    return this;
  }

   /**
   * Get categoryCode
   * @return categoryCode
  **/
  @Schema(description = "")
  public String getCategoryCode() {
    return categoryCode;
  }

  public void setCategoryCode(String categoryCode) {
    this.categoryCode = categoryCode;
  }

  public Profession salutationCode(String salutationCode) {
    this.salutationCode = salutationCode;
    return this;
  }

   /**
   * Get salutationCode
   * @return salutationCode
  **/
  @Schema(description = "")
  public String getSalutationCode() {
    return salutationCode;
  }

  public void setSalutationCode(String salutationCode) {
    this.salutationCode = salutationCode;
  }

  public Profession lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

   /**
   * Get lastName
   * @return lastName
  **/
  @Schema(description = "")
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Profession firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

   /**
   * Get firstName
   * @return firstName
  **/
  @Schema(description = "")
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public Profession expertises(List<Expertise> expertises) {
    this.expertises = expertises;
    return this;
  }

  public Profession addExpertisesItem(Expertise expertisesItem) {
    if (this.expertises == null) {
      this.expertises = new ArrayList<Expertise>();
    }
    this.expertises.add(expertisesItem);
    return this;
  }

   /**
   * Get expertises
   * @return expertises
  **/
  @Valid
  @Schema(description = "")
  public List<Expertise> getExpertises() {
    return expertises;
  }

  public void setExpertises(List<Expertise> expertises) {
    this.expertises = expertises;
  }

  public Profession workSituations(List<WorkSituation> workSituations) {
    this.workSituations = workSituations;
    return this;
  }

  public Profession addWorkSituationsItem(WorkSituation workSituationsItem) {
    if (this.workSituations == null) {
      this.workSituations = new ArrayList<WorkSituation>();
    }
    this.workSituations.add(workSituationsItem);
    return this;
  }

   /**
   * Get workSituations
   * @return workSituations
  **/
  @Valid
  @Schema(description = "")
  public List<WorkSituation> getWorkSituations() {
    return workSituations;
  }

  public void setWorkSituations(List<WorkSituation> workSituations) {
    this.workSituations = workSituations;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Profession profession = (Profession) o;
    return Objects.equals(this.exProId, profession.exProId) &&
        Objects.equals(this.code, profession.code) &&
        Objects.equals(this.categoryCode, profession.categoryCode) &&
        Objects.equals(this.salutationCode, profession.salutationCode) &&
        Objects.equals(this.lastName, profession.lastName) &&
        Objects.equals(this.firstName, profession.firstName) &&
        Objects.equals(this.expertises, profession.expertises) &&
        Objects.equals(this.workSituations, profession.workSituations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exProId, code, categoryCode, salutationCode, lastName, firstName, expertises, workSituations);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Profession {\n");
    
    sb.append("    exProId: ").append(toIndentedString(exProId)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    categoryCode: ").append(toIndentedString(categoryCode)).append("\n");
    sb.append("    salutationCode: ").append(toIndentedString(salutationCode)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    expertises: ").append(toIndentedString(expertises)).append("\n");
    sb.append("    workSituations: ").append(toIndentedString(workSituations)).append("\n");
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
