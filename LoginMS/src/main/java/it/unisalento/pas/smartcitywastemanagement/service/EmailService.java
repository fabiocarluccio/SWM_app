package it.unisalento.pas.smartcitywastemanagement.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SimpleMailMessage templateCredentialsMessage;

    public void sendCredentialsEmail(String recipient, String username, String password) throws MailException {

        SimpleMailMessage message = new SimpleMailMessage(templateCredentialsMessage);

        // Setting del messaggio da inviare
        String text = String.format(templateCredentialsMessage.getText(), username, password);
        message.setText(text);
        message.setTo("carmine.accogli01@gmail.com"); // qua va recipient

        // Invio della mail
        mailSender.send(message);

    }







}
