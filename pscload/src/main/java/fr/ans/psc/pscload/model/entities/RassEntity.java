/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.entities;

/**
 * The Interface RassEntity.
 */
public interface RassEntity {

	/**
	 * Gets the return status.
	 *
	 * @return the return status
	 */
	int getReturnStatus();

	/**
	 * Sets the return status.
	 *
	 * @param returnStatus the new return status
	 */
	void setReturnStatus(int returnStatus);
	
	/**
	 * Gets the internal id.
	 *
	 * @return the internal id
	 */
	String getInternalId();
	
	/**
	 * Gets the id type.
	 *
	 * @return the id type
	 */
	String getIdType();

}