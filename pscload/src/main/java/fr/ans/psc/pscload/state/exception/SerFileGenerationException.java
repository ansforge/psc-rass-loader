package fr.ans.psc.pscload.state.exception;

public class SerFileGenerationException extends LoadProcessException {
    private static final long serialVersionUID = 8223931257333919635L;

    public SerFileGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerFileGenerationException(Throwable cause) {
        super(cause);
    }

    public SerFileGenerationException() {
    }

    public SerFileGenerationException(String message) {
        super(message);
    }
}
