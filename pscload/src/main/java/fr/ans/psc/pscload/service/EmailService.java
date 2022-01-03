/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * The Class EmailService.
 */
@Service
@Slf4j
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
     * @param subject        the email nature
     * @param body           the body of the mail
     * @param attachmentFile an optional attachment file
     */
    public void sendMail(String subject, String body, File attachmentFile) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(sender);
            String[] allReceivers = receiver.split(",");
            helper.setTo(allReceivers);
            helper.setSubject(subject);

            Multipart emailContent = new MimeMultipart();
            MimeBodyPart textBody = new MimeBodyPart();
            textBody.setText(body);
            emailContent.addBodyPart(textBody);

            if (attachmentFile != null) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachmentFile);
                emailContent.addBodyPart(attachmentPart);
            }

            message.setContent(emailContent);
            emailSender.send(message);
        } catch (MailSendException | MessagingException | IOException mse) {
            log.error("Mail sending error", mse);
        }
    }

}
