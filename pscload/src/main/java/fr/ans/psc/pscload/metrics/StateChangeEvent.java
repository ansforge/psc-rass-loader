/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.metrics;

import fr.ans.psc.pscload.model.EmailTemplate;
import org.springframework.context.ApplicationEvent;

import java.io.File;

/**
 * The Class StateChangeEvent.
 */
public class StateChangeEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2867706902828846647L;

	private final int stage;
	private EmailTemplate emailTemplate;
	private final String message;
	private File attachmentFile;

	/**
	 * Instantiates a new state change event.
	 *
	 * @param source the source
	 * @param stage the stage
	 * @param emailTemplate the email template
	 * @param message the message
	 * @param attachmentFile the attachment file
	 */
	public StateChangeEvent(Object source, int stage, EmailTemplate emailTemplate, String message, File attachmentFile) {
		super(source);
		this.stage = stage;
		this.emailTemplate = emailTemplate;
		this.message = message;
		this.attachmentFile = attachmentFile;
	}

	public int getStage() {
		return stage;
	}

	public String getMessage() {
		return message;
	}

	public File getAttachmentFile() { return attachmentFile; }

	public EmailTemplate getEmailNature() {
		return emailTemplate;
	}
}
