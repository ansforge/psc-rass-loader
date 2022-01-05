/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.visitor;

import fr.ans.psc.pscload.model.operations.PsCreateMap;
import fr.ans.psc.pscload.model.operations.PsDeleteMap;
import fr.ans.psc.pscload.model.operations.PsUpdateMap;
import fr.ans.psc.pscload.model.operations.StructureCreateMap;
import fr.ans.psc.pscload.model.operations.StructureUpdateMap;

/**
 * The Interface MapsUploaderVisitor.
 */
public interface MapsVisitor {

	/**
	 * Visit.
	 *
	 * @param map the map
	 */
	void visit(PsCreateMap map);
	
	/**
	 * Visit.
	 *
	 * @param map the map
	 */
	void visit(PsDeleteMap map);
	
	/**
	 * Visit.
	 *
	 * @param map the map
	 */
	void visit(PsUpdateMap map);
	
	/**
	 * Visit.
	 *
	 * @param map the map
	 */
	void visit(StructureCreateMap map);
	
	/**
	 * Visit.
	 *
	 * @param map the map
	 */
	void visit(StructureUpdateMap map);
	
}
