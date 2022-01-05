/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.visitor;

import java.util.List;

import fr.ans.psc.pscload.model.operations.PsCreateMap;
import fr.ans.psc.pscload.model.operations.PsDeleteMap;
import fr.ans.psc.pscload.model.operations.PsUpdateMap;
import fr.ans.psc.pscload.model.operations.StructureCreateMap;
import fr.ans.psc.pscload.model.operations.StructureUpdateMap;

/**
 * The Interface MapsCleanerVisitor.
 */
public interface MapsCleanerVisitor {

	/**
	 * Visit.
	 *
	 * @param map the map
	 * @return the list
	 */
	List<String> visit(PsCreateMap map);

	/**
	 * Visit.
	 *
	 * @param map the map
	 * @return the list
	 */
	List<String> visit(PsUpdateMap map);

	/**
	 * Visit.
	 *
	 * @param map the map
	 * @return the list
	 */
	List<String> visit(PsDeleteMap map);

	/**
	 * Visit.
	 *
	 * @param map the map
	 * @return the list
	 */
	List<String> visit(StructureCreateMap map);

	/**
	 * Visit.
	 *
	 * @param map the map
	 * @return the list
	 */
	List<String> visit(StructureUpdateMap map);

}
