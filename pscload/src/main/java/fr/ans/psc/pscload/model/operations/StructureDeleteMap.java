package fr.ans.psc.pscload.model.operations;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StructureDeleteMap implements OperationMap<String, RassEntity> {

    private Map<String , RassEntity> newValues;

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
    public Map<String, RassEntity> getNewValues() {
        if (newValues == null) {
            newValues = new ConcurrentHashMap<>();
        }
        return newValues;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObjectOrNull(output, newValues, ConcurrentHashMap.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        newValues = (Map<String, RassEntity>) kryo.readObjectOrNull(input, ConcurrentHashMap.class);
    }
}
