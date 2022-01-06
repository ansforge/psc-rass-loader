package fr.ans.psc.pscload.model;

public enum Stage {
    FINISHED(0),
    SUBMITTED(10),
    READY_TO_EXTRACT(20),
    READY_TO_COMPUTE(30),
    DIFF_COMPUTED(50),
    UPLOAD_CHANGES_STARTED(60),
    UPLOAD_CHANGES_FINISHED(70),
    CURRENT_MAP_SERIALIZED(80);

    public int value;

    Stage(int value) { this.value = value; }
}
