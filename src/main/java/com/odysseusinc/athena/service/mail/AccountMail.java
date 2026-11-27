package com.odysseusinc.athena.service.mail;

import java.net.URI;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

/** SES transport for account emails. Spring Boot still manages the notification mail sender. */
@Service
public class AccountMail {
    private final JavaMailSenderImpl smtp = new JavaMailSenderImpl();
    public record Recipient(String email, String firstName) {}

    private final String from;
    private final MailContentBuilder templates;

    public AccountMail(@Value("${athena.ses.host:email-smtp.us-west-2.amazonaws.com}") String host,
                       @Value("${athena.ses.port:587}") int port,
                       @Value("${athena.ses.username:}") String username,
                       @Value("${athena.ses.password:}") String password,
                       @Value("${athena.ses.from:}") String from, MailContentBuilder templates) {
        this.templates = templates;
        this.from = from;
        smtp.setHost(host);
        smtp.setPort(port);
        smtp.setUsername(username);
        smtp.setPassword(password);
        smtp.setDefaultEncoding("UTF-8");
        var properties = smtp.getJavaMailProperties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.starttls.required", "true");
        properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        properties.setProperty("mail.smtp.connectiontimeout", "5000");
        properties.setProperty("mail.smtp.timeout", "10000");
        properties.setProperty("mail.smtp.writetimeout", "10000");
    }

    public void sendConfirmation(Recipient recipient, URI link) {
        send(recipient, link, "Confirm your Athena account", "mail/registration",
                "Thank you for registering with Athena. Confirm your email address within 24 hours.",
                "If the link expires, register again with the same email address and password.");
    }

    public void sendPasswordReset(Recipient recipient, URI link) {
        send(recipient, link, "Reset your Athena password", "mail/remind_password",
                "We received a request to reset your Athena password. Choose a new password within 30 minutes.",
                "If you did not request a password reset, you can ignore this email.");
    }

    private void send(Recipient recipient, URI link, String subject, String template,
                      String instructions, String footer) {
        if (from.isBlank() || smtp.getUsername().isBlank() || smtp.getPassword().isBlank()) {
            throw new MailSendException("Athena account email is not configured.");
        }
        String greeting = recipient.firstName() == null || recipient.firstName().isBlank()
                ? "Hello," : "Hello " + recipient.firstName().strip() + ",";
        String url = link.toASCIIString();
        String text = String.join("\n\n", greeting, instructions, url, footer, "The Athena team");
        smtp.send(mime -> {
            var message = new MimeMessageHelper(mime, MimeMessageHelper.MULTIPART_MODE_MIXED, "UTF-8");
            message.setFrom(from);
            message.setTo(recipient.email());
            message.setSubject(subject);
            String html = templates.build(template, Map.of("greeting", greeting, "instructions", instructions,
                    "link", url, "footer", footer));
            message.setText(text, html);
        });
    }
}
