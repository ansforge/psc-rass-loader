/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state.exception;

/**
 * The Class ExtractTriggeringException.
 */
public class ExtractTriggeringException extends LoadProcessException {
    private static final long serialVersionUID = 8223931257222919635L;

    /**
     * Instantiates a new extract triggering exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ExtractTriggeringException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new extract triggering exception.
     *
     * @param cause the cause
     */
    public ExtractTriggeringException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new extract triggering exception.
     */
    public ExtractTriggeringException() {
    }

    /**
     * Instantiates a new extract triggering exception.
     *
     * @param message the message
     */
    public ExtractTriggeringException(String message) {
        super(message);
    }
}
