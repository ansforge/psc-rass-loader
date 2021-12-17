/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state.exception;

/**
 * The Class UploadException.
 */
public class UploadException extends LoadProcessException {



	/**
	 * 
	 */
	private static final long serialVersionUID = -9145932753460359258L;

	/**
	 * Instantiates a new Upload exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public UploadException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new Upload exception.
	 *
	 * @param cause the cause
	 */
	public UploadException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new Upload exception.
	 */
	public UploadException() {
		super();
	}

	/**
	 * Instantiates a new Upload exception.
	 *
	 * @param message the message
	 */
	public UploadException(String message) {
		super(message);
	}

}
