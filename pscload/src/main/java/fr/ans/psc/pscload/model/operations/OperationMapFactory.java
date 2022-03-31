/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;

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
            case CREATE:
                map = new PsCreateMap(OperationType.CREATE);
                break;
            case DELETE:
                map = new PsDeleteMap(OperationType.DELETE);
                break;
            case UPDATE:
                map = new PsUpdateMap(OperationType.UPDATE);
                break;
            default:
                map = null;
                break;
        }
        return map;

    }
}
