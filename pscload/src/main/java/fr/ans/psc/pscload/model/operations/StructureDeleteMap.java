package fr.ans.psc.pscload.model.operations;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StructureDeleteMap extends OperationMap<String, RassEntity> {

    private Map<String , RassEntity> newValues;

    public StructureDeleteMap() {
        super();
    }

    public StructureDeleteMap(OperationType operation) {
        super(operation);
    }


    @Override
    public OperationType getOperation() {
        return OperationType.STRUCTURE_DELETE;
    }

    @Override
    public void accept(MapsVisitor visitor) {
        visitor.visit(this);
    }
}
