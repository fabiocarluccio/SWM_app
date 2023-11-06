package it.unisalento.pas.smartcitywastemanagement.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("wastemanagementapp.info@gmail.com");
        mailSender.setPassword("njkocgwejsbsczqd");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }


    // BEAN per la definizione del template email per comunicare le credenziali di accesso al cittadino
    @Bean
    public SimpleMailMessage templateCredentialsMessage() {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setText(
                "Hey you \uD83D\uDC40,\n" +
                        "Congratulations! You're now part of our Trash Busters community, and your trashy adventure is about to begin with us! ♻\uFE0F\uD83D\uDDD1\uFE0F\n\n" +
                        "Hold tight, here are your secret access codes:\n" +
                        "- Trash Hero Name: %s\n" +
                        "- Initial Trashy Password: %s\n\n" +
                        "Remember, change this password at your first login!\n\n" +
                        "Thanks for joining us on this waste-reducing rendezvous!\n"+
                        "Let's make this planet a cleaner place, one bin at a time! \uD83D\uDEAE\uD83C\uDF0E\n\n\n" +
                        "Trashy high-fives \uD83D\uDD90\uFE0F,\n" +
                        "The Cleanup Crew");
         message.setSubject("Your trashy account credentials");

        return message;
    }

    @Bean
    public SimpleMailMessage templateResetPasswordMessage() {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setText(
                "Hey you \uD83D\uDC40,\n" +
                        "Your password change request has been granted. \n"+
                        "We've generated a special secret token just for you. Here it is:\n"+
                        "\uD83D\uDD10 Password Change Token: %s \n\n"+
                        "Create something unique and secure, so no cyber-villains can defeat you\n"+
                        "And, of course, keep this new password safe!\n\n\n"+
                        "Trashy high-fives \uD83D\uDD90\uFE0F,\n" +
                        "The Cleanup Crew");
        message.setSubject("Lost Your Password? Let's Recycle It!");

        return message;
    }
}
