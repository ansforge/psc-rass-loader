package fr.ans.psc.pscload.visitor;

import fr.ans.psc.pscload.model.OperationMap;
import fr.ans.psc.pscload.model.RassEntity;

public interface MapsUploaderVisitor {

	void visit(OperationMap<String, RassEntity> map);
	
}
