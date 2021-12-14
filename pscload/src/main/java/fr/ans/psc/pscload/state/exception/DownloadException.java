/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state.exception;

/**
 * The Class DownloadException.
 */
public class DownloadException extends LoadProcessException {

	/**
	 * Instantiates a new download exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new download exception.
	 *
	 * @param cause the cause
	 */
	public DownloadException(Throwable cause) {
		super(cause);
	}

}
