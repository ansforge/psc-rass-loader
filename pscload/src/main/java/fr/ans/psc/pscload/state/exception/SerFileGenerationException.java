/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state.exception;

/**
 * The Class SerFileGenerationException.
 */
public class SerFileGenerationException extends LoadProcessException {
    private static final long serialVersionUID = 8223931257333919635L;

    /**
     * Instantiates a new ser file generation exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public SerFileGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new ser file generation exception.
     *
     * @param cause the cause
     */
    public SerFileGenerationException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new ser file generation exception.
     */
    public SerFileGenerationException() {
    }

    /**
     * Instantiates a new ser file generation exception.
     *
     * @param message the message
     */
    public SerFileGenerationException(String message) {
        super(message);
    }
}
