package fr.ans.psc.pscload.metrics;

import org.springframework.context.ApplicationEvent;

public class StateChangeEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2867706902828846647L;

	private final int stage;
	private final String message;

	public StateChangeEvent(Object source, int stage, String message) {
		super(source);
		this.stage = stage;
		this.message = message;
	}

	public int getStage() {
		return stage;
	}

	public String getMessage() {
		return message;
	}

}