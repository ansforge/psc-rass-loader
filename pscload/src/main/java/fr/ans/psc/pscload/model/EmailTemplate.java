/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model;

/**
 * The Enum EmailNature.
 */
public enum EmailTemplate {

    /**
     * The process has finished successfully.
     */
    PROCESS_FINISHED("PSCLOAD - Fin de process", "Le process pscload s'est terminé correctement."),

    /**
     * The process has been interrupting while uploading changes
     */
    UPLOAD_REST_INTERRUPTION("PSCLOAD - Interruption du chargement des modifications",
            "Le chargement des modifications de pscload a été interrompu avant son terme.\n" +
                    "Vous pouvez l'annuler ou forcer sa reprise manuellement.\n" +
                    "En l'absence d'action de votre part il sera abandonné à l'issue du délai de reprise paramétré."),

    /**
     * The process has finished with incomplete changes uploading.
     */
    UPLOAD_INCOMPLETE("PSCLOAD - Chargement incomplet des modifications",
            "Les modifications du RASS ont été partiellement traitées."),

    /**
     * The process has been interrupted
     */
    TRIGGER_EXTRACT_FAILED("PSCLOAD - échec de la génération de l'extract",
            "Le chargement des modifications de pscload a bien eu lieu mais la génération de l'extract sécurisé" +
                    " n'a pas été déclenchée.\n" +
                    "Veuillez vérifier l'état de la plate-forme et redéclenchez-la manuellement"),

    /**
     * The process has been interrupted
     */
    INTERRUPTED_PROCESS("PSCLOAD - interruption du process",
            "Le process pscload a été interrompu avant son terme.\n Il sera redéclenché au prochain horaire" +
                    " paramétré."),

    /**
     * The process has been interrupted after uploading changes by a (de)serialization error
     */
    SERIALIZATION_FAILURE("PSCLOAD - interruption du process",
            "Le process pscload a été interrompu avant génération du fichier sérialisé.\n Vous pouvez relancer " +
                    "l'action \"ending-operations\" manuellement");
    /**
     * The subject.
     */
    public String subject;
    /**
     * The default message
     */
    public String message;

    /**
     * Instantiates a new email nature.
     *
     * @param subject the subject
     * @param message the default message
     */
    EmailTemplate(String subject, String message) {
        this.subject = subject;
        this.message = message;
    }
}
