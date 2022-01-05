package fr.ans.psc.pscload.visitor;

import java.util.List;

import fr.ans.psc.pscload.model.OperationMap;
import fr.ans.psc.pscload.model.RassEntity;

public interface MapsCleanerVisitor {

	List<String> visit(OperationMap<String, RassEntity> map);
	
}
