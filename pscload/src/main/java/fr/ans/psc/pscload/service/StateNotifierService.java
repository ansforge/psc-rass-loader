/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import fr.ans.psc.pscload.metrics.StateChangeEvent;

/**
 * The Class EmailService.
 */
@Service
public class StateNotifierService implements ApplicationListener<StateChangeEvent> {

    @Autowired
    private EmailService emailService;

	@Override
	public void onApplicationEvent(StateChangeEvent event) {
		emailService.sendMail(EmailNature.STATE_CHANGED, event.getMessage());	
	}
}
