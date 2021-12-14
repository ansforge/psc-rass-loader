/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state.exception;

/**
 * The Class ExtractException.
 */
public class ExtractException extends LoadProcessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8360043811806913205L;

	/**
	 * Instantiates a new extract exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public ExtractException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new extract exception.
	 *
	 * @param cause the cause
	 */
	public ExtractException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new extract exception.
	 */
	public ExtractException() {
		super();
	}

	/**
	 * Instantiates a new extract exception.
	 *
	 * @param message the message
	 */
	public ExtractException(String message) {
		super(message);
	}

}
