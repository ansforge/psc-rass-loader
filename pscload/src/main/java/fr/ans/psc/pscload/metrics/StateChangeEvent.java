package fr.ans.psc.pscload.metrics;

import fr.ans.psc.pscload.service.EmailTemplate;
import org.springframework.context.ApplicationEvent;

public class StateChangeEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2867706902828846647L;

	private final int stage;
	private EmailTemplate emailTemplate;
	private final String message;

	public StateChangeEvent(Object source, int stage, EmailTemplate emailTemplate, String message) {
		super(source);
		this.stage = stage;
		this.emailTemplate = emailTemplate;
		this.message = message;
	}

	public int getStage() {
		return stage;
	}

	public String getMessage() {
		return message;
	}

	public EmailTemplate getEmailNature() {
		return emailTemplate;
	}
}
