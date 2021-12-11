package fr.ans.psc.pscload.state.exception;

public class ExtractException extends LoadProcessException {

	public ExtractException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtractException(Throwable cause) {
		super(cause);
	}

	public ExtractException() {
		super();
	}

	public ExtractException(String message) {
		super(message);
	}

}
