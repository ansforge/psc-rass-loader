package fr.ans.psc.pscload.state.exception;

public class ExtractTriggeringException extends LoadProcessException {
    private static final long serialVersionUID = 8223931257222919635L;

    public ExtractTriggeringException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtractTriggeringException(Throwable cause) {
        super(cause);
    }

    public ExtractTriggeringException() {
    }

    public ExtractTriggeringException(String message) {
        super(message);
    }
}
