/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.OperationType;

/**
 * A factory for creating OperationMap objects.
 */
public class OperationMapFactory {

    /**
     * Gets the operation map.
     *
     * @param operation the operation
     * @return the operation map
     */
    public static OperationMap<String, RassEntity> getOperationMap(OperationType operation) {
        OperationMap<String, RassEntity> map;
        switch (operation) {
            case PS_CREATE:
                map = new PsCreateMap();
                break;
            case PS_DELETE:
                map = new PsDeleteMap();
                break;
            case PS_UPDATE:
                map = new PsUpdateMap();
                break;
            case STRUCTURE_CREATE:
                map = new StructureCreateMap();
                break;
            case STRUCTURE_UPDATE:
                map = new StructureUpdateMap();
                break;
            case STRUCTURE_DELETE:
                map = new StructureDeleteMap();
                break;
            default:
                map = null;
                break;
        }
        return map;

    }
}
