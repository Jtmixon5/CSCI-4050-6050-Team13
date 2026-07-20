package com.cinema.ebooking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class RegistrationEmailService {

    private static final Logger log =
        LoggerFactory.getLogger(RegistrationEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String frontendUrl;
    private final String mailFrom;
    private final String mailHost;

    public RegistrationEmailService(
        ObjectProvider<JavaMailSender> mailSenderProvider,
        @Value("${app.frontend-url}") String frontendUrl,
        @Value("${app.mail-from}") String mailFrom,
        @Value("${spring.mail.host:}") String mailHost
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.frontendUrl = frontendUrl;
        this.mailFrom = mailFrom;
        this.mailHost = mailHost;
    }

    public void sendConfirmation(String recipient, String rawToken) {
        String confirmationUrl =
            frontendUrl + "/verify-email?token=" + rawToken;

        send(
            recipient,
            "Confirm your Cinema E-Booking account",
            "Welcome to Cinema E-Booking!\n\n"
                + "Confirm your email address by opening this link:\n"
                + confirmationUrl
                + "\n\nThis link expires in 24 hours."
        );
    }

    public void sendPasswordReset(String recipient, String rawToken) {
        String resetUrl =
            frontendUrl + "/reset-password?token=" + rawToken;
        send(
            recipient,
            "Reset your Cinema E-Booking password",
            "A password reset was requested for your account.\n\n"
                + "Reset your password using this link:\n"
                + resetUrl
                + "\n\nThis link expires in one hour. "
                + "If you did not request it, ignore this message."
        );
    }

    public void sendProfileChanged(String recipient) {
        send(
            recipient,
            "Your Cinema E-Booking profile was updated",
            "Your personal profile information was changed. "
                + "If you did not make this change, contact support immediately."
        );
    }

    private void send(String recipient, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailHost.isBlank() || mailSender == null) {
            log.warn("SMTP is not configured. Email '{}' to {}: {}", subject, recipient, body);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
