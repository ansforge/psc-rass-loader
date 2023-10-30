/**
 * Copyright (C) 2022-2023 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscload.model.operations;

import java.util.concurrent.ConcurrentHashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import fr.ans.psc.pscload.model.entities.RassEntity;

/**
 * The Class OperationMapSerializer.
 */
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
