package fr.ans.psc.pscload.metrics;

import fr.ans.psc.pscload.service.EmailNature;
import org.springframework.context.ApplicationEvent;

public class StateChangeEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2867706902828846647L;

	private final int stage;
	private EmailNature emailNature;
	private final String message;

	public StateChangeEvent(Object source, int stage, EmailNature emailNature, String message) {
		super(source);
		this.stage = stage;
		this.emailNature = emailNature;
		this.message = message;
	}

	public int getStage() {
		return stage;
	}

	public String getMessage() {
		return message;
	}

	public EmailNature getEmailNature() {
		return emailNature;
	}
}
