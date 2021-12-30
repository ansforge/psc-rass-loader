/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.service;

import java.io.File;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * The Class EmailService.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

	@Value("${spring.mail.username}")
    private String sender;

    @Value("${pscload.mail.receiver}")
    private String receiver;

    
    public void setEmailSender(JavaMailSender emailSender) {
		this.emailSender = emailSender;
	}

    /**
     * Send mail.
     *
     * @param subject the email nature
     * @param body the body of the mail
     */
    public void sendMail(String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        String[] allReceivers = receiver.split(",");
        message.setTo(allReceivers);
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }

}
