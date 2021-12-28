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

package fr.ans.psc.pscload.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class ProcessInfo implements Serializable{
  private static final long serialVersionUID = 1L;
  
  
  @JsonProperty("processId")
  private String processId;

  @JsonProperty("createdOn")
  private String createdOn;
  
  @JsonProperty("state")
  private String state;
  
  @JsonProperty("psToCreate")
  private int psToCreate;
  
  @JsonProperty("psToUpdate")
  private int psToUpdate;
  
  @JsonProperty("psToDelete")
  private int psToDelete;
  
  @JsonProperty("structureToCreate")
  private int structureToCreate;
  
  @JsonProperty("structureToUpdate")
  private int structureToUpdate;

}