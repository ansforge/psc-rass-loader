/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model;

/**
 * The Enum Stage.
 */
public enum Stage {
    
    /** The finished. */
    FINISHED(0),
    
    /** The submitted. */
    SUBMITTED(10),
    
    /** The ready to extract. */
    READY_TO_EXTRACT(20),
    
    /** The ready to compute. */
    READY_TO_COMPUTE(30),
    
    /** The diff computed. */
    DIFF_COMPUTED(50),
    
    /** The upload changes started. */
    UPLOAD_CHANGES_STARTED(60),
    
    /** The upload changes finished. */
    UPLOAD_CHANGES_FINISHED(70),
    
    /** The current map serialized. */
    CURRENT_MAP_SERIALIZED(80);

    /** The value. */
    public int value;

    /**
     * Instantiates a new stage.
     *
     * @param value the value
     */
    Stage(int value) { this.value = value; }
}
