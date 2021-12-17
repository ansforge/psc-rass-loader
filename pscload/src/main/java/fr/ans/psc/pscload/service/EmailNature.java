/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.service;

/**
 * The Enum EmailNature.
 */
public enum EmailNature {
    
    /** The process finished. */
    PROCESS_FINISHED("Le process pscload s'est terminé, le fichier %s a été généré à partir du fichier %s.", "PSCLOAD - Fin de process"),
    
    /** The process relaunched. */
    PROCESS_RELAUNCHED("Le fichier %s n'est pas cohérent avec le fichier %s. Reprise du process.", "PSCLOAD - Reprise du process"),
	
	STATE_CHANGED("L'étape a changé","PSCLOAD State change");

    /** The message. */
    public String message;
    
    /** The subject. */
    public String subject;

    /**
     * Instantiates a new email nature.
     *
     * @param message the message
     * @param subject the subject
     */
    EmailNature(String message, String subject) {this.message = message; this.subject = subject;}
}
