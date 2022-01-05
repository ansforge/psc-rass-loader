/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.visitor;

/**
 * The Interface Visitable.
 */
public interface Visitable {

	/**
	 * Accept.
	 *
	 * @param visitor the visitor
	 */
	public void accept(MapsVisitor visitor);
}