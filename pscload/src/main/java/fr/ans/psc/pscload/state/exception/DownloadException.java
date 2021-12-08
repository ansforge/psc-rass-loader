package fr.ans.psc.pscload.state.exception;

public class DownloadException extends LoadProcessException {

	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}

	public DownloadException(Throwable cause) {
		super(cause);
	}

}
