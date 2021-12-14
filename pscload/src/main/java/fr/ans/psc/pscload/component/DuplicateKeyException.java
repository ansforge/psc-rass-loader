/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.component;

/**
 * The Class DuplicateKeyException.
 */
public class DuplicateKeyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2055488226238329069L;

	/**
	 * Instantiates a new duplicate key exception.
	 */
	public DuplicateKeyException() {
		super();
	}

	/**
	 * Instantiates a new duplicate key exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public DuplicateKeyException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new duplicate key exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public DuplicateKeyException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new duplicate key exception.
	 *
	 * @param message the message
	 */
	public DuplicateKeyException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new duplicate key exception.
	 *
	 * @param cause the cause
	 */
	public DuplicateKeyException(Throwable cause) {
		super(cause);
	}

}
