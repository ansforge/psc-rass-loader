/**
 * Copyright (C) 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
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
package fr.ans.psc.pscload.model;

/**
 * The Enum EmailNature.
 */
public enum EmailTemplate {

    /**
     * The process has been interrupting while uploading changes
     */
    UPLOAD_REST_INTERRUPTION("PSCLOAD - Interruption du chargement des modifications",
            "Le chargement des modifications de pscload a été interrompu avant son terme.\n" +
                    "Vous pouvez l'annuler ou forcer sa reprise manuellement.\n"),

    /**
     * The application is restarting with a process at upload interrupted state
     */
    UPLOAD_INTERRUPTED_RESTART("PSCLOAD - redémarrage avec modifications en attente",
            "L'application pscload vient de redémarrer alors que le précédent chargement des modifications avait" +
                    "été interrompu avant son terme.\n" +
                    "Vous pouvez l'annuler ou forcer sa reprise manuellement."),

    /**
     * The process has finished.
     */
    UPLOAD_FINISHED("PSCLOAD - Fin de process", "Les modifications du RASS ont été partiellement traitées."),

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
