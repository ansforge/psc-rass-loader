package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

public class StructureDeleteMap extends OperationMap<String, RassEntity> {

    public StructureDeleteMap() {
    }

    public StructureDeleteMap(OperationType operation) {
        super(operation);
    }

    @Override
    public void accept(MapsVisitor visitor) {
        visitor.visit(this);
    }
}
