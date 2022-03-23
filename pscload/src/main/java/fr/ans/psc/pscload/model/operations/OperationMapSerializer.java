/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import fr.ans.psc.pscload.model.entities.RassEntity;

import java.util.concurrent.ConcurrentHashMap;

public class OperationMapSerializer extends MapSerializer<OperationMap> {

    @Override
    public void write(Kryo kryo, Output output, OperationMap map) {
        super.write(kryo, output, map);
        kryo.writeObjectOrNull(output, map.getOldValues(), ConcurrentHashMap.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public OperationMap<String, RassEntity> read(Kryo kryo, Input input, Class<? extends OperationMap> type) {
        OperationMap<String, RassEntity> map = super.read(kryo, input, type);
        map.setOldValues(kryo.readObjectOrNull(input, ConcurrentHashMap.class));
        return map;
    }
}
