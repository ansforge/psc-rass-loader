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
     * @param emailNature the email nature
     * @param latestTxtAndSer the latest txt and ser
     */
    public void sendMail(EmailNature emailNature, Map<String, File> latestTxtAndSer) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        String[] allReceivers = receiver.split(",");
        message.setTo(allReceivers);
        message.setSubject(emailNature.subject);
        message.setText(getEmailMessage(emailNature, latestTxtAndSer));

        emailSender.send(message);
    }

    /**
     * Send mail.
     *
     * @param emailNature the email nature
     * @param latestTxtAndSer the latest txt and ser
     */
    public void sendMail(EmailNature emailNature, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        String[] allReceivers = receiver.split(",");
        message.setTo(allReceivers);
        message.setSubject(emailNature.subject);
        message.setText(body);
        emailSender.send(message);
    }
    
    private String getEmailMessage(EmailNature emailNature, Map<String, File> latestTxtAndSer) {
        String latestTxt = latestTxtAndSer.get("txt").getName();
        String latestSer = latestTxtAndSer.get("ser").getName();

        return String.format(emailNature.message, latestSer, latestTxt);
    }

}
