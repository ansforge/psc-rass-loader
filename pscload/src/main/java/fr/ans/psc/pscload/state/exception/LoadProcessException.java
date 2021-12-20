/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state.exception;

/**
 * The Class LoadProcessException.
 */
public class LoadProcessException extends RuntimeException {



	/**
	 * Instantiates a new load process exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public LoadProcessException(String message, Throwable cause) {
		super(message, cause);
	}


	/**
	 * Instantiates a new load process exception.
	 *
	 * @param cause the cause
	 */
	public LoadProcessException(Throwable cause) {
		super(cause);
	}


	/**
	 * Instantiates a new load process exception.
	 */
	public LoadProcessException() {
		super();
	}


	/**
	 * Instantiates a new load process exception.
	 *
	 * @param message the message
	 */
	public LoadProcessException(String message) {
		super(message);
	}

	
}
