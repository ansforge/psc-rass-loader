/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.visitor;

import java.util.List;

/**
 * The Interface Visitable.
 */
public interface Visitable {

	/**
	 * Accept.
	 *
	 * @param visitor the visitor
	 * @return the list
	 */
	public List<String> accept(MapsCleanerVisitor visitor);
	
	/**
	 * Accept.
	 *
	 * @param visitor the visitor
	 */
	public void accept(MapsUploaderVisitor visitor);

}