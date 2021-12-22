package fr.ans.psc.pscload.state.exception;

public class ChangesApplicationException extends LoadProcessException {
    private static final long serialVersionUID = 8223931257333919635L;

    public ChangesApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChangesApplicationException(Throwable cause) {
        super(cause);
    }

    public ChangesApplicationException() {
    }

    public ChangesApplicationException(String message) {
        super(message);
    }
}
