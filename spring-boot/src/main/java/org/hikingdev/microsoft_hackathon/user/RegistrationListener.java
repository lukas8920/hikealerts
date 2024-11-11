package org.hikingdev.microsoft_hackathon.user;

import org.hikingdev.microsoft_hackathon.user.entities.OnRegistrationCompleteEvent;
import org.hikingdev.microsoft_hackathon.user.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    private final AuthService authService;
    private final JavaMailSender mailSender;

    @Value("${server.host}")
    private String host;
    @Value("${springboot.server.port}")
    private String port;
    @Value("${contact.mail.address}")
    private String contactMail;

    @Autowired
    public RegistrationListener(AuthService authService, JavaMailSender mailSender){
        this.authService = authService;
        this.mailSender = mailSender;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        authService.createVerificationToken(user, token);

        String recipientAddress = user.getMail();
        String subject = "Registration Confirmation";
        String confirmationUrl
                = event.getAppUrl() + "/registration_confirm?token=" + token;
        String message = "Please click on the link to complete the registration of your account.";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(contactMail);
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + "\r\n" + this.host + ":" + port + confirmationUrl);
        mailSender.send(email);
    }
}
