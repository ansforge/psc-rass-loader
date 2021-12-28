/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import fr.ans.psc.pscload.metrics.StateChangeEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class EmailService.
 */
@Slf4j
@Service
public class StateNotifierService implements ApplicationListener<StateChangeEvent> {

    @Autowired
    private EmailService emailService;

	@Override
	public void onApplicationEvent(StateChangeEvent event) {
		log.info(event.getMessage());
		emailService.sendMail(event.getEmailNature().subject, event.getMessage());
	}
}
