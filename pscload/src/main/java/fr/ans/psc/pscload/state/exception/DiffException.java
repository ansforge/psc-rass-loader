/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state.exception;

/**
 * The Class DiffException.
 */
public class DiffException extends LoadProcessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8223931257973919625L;

	/**
	 * Instantiates a new diff exception.
	 */
	public DiffException() {
		super();
	}

	/**
	 * Instantiates a new diff exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public DiffException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new diff exception.
	 *
	 * @param message the message
	 */
	public DiffException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new diff exception.
	 *
	 * @param cause the cause
	 */
	public DiffException(Throwable cause) {
		super(cause);
	}

}
