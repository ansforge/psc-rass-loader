package fr.ans.psc.pscload.state.exception;

public class ExtractException extends LoadProcessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8360043811806913205L;

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
