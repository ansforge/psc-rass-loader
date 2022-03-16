package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StructureDeleteMap implements OperationMap<String, RassEntity> {

    private ConcurrentMap<String , RassEntity> newValues;

    public StructureDeleteMap() {
    }

    @Override
    public OperationType getOperation() {
        return OperationType.STRUCTURE_DELETE;
    }

    @Override
    public void accept(MapsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveNewValue(String key, RassEntity value) {
        if (newValues == null) {
            newValues = new ConcurrentHashMap<>();
        }
        newValues.put(key, value);
    }

    @Override
    public RassEntity getNewValue(String key) {
        if (newValues == null) {
            newValues = new ConcurrentHashMap<>();
        }
        return newValues.get(key);
    }

    @Override
    public ConcurrentMap<String, RassEntity> getNewValues() {
        if (newValues == null) {
            newValues = new ConcurrentHashMap<>();
        }
        return newValues;
    }
}
