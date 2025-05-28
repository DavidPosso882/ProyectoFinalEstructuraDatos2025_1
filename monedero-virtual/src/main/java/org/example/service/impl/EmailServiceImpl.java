package org.example.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementation of the EmailService interface.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Monedero Virtual}")
    private String appName;

    @Override
    @Async
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("Email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
            // Still log the message content for backup
            logMessageContent(to, subject, text);
        }
    }

    @Override
    @Async
    public void sendHtmlMessage(String to, String subject, String html) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            emailSender.send(message);
            logger.info("HTML email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send HTML email to: {}", to, e);
            // Still log the message content for backup
            logMessageContent(to, subject, html);
        }
    }

    @Override
    public void sendVerificationToken(String to, String token) {
        String subject = appName + " - Código de verificación";
        String htmlContent = createVerificationTokenEmailContent(token);
        sendHtmlMessage(to, subject, htmlContent);
    }

    /**
     * Creates HTML content for verification token email.
     *
     * @param token Verification token
     * @return HTML content
     */
    private String createVerificationTokenEmailContent(String token) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>" +
                "<h2 style='color: #4285f4;'>" + appName + " - Verificación</h2>" +
                "<p>Tu código de verificación es:</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 4px; text-align: center; font-size: 24px; letter-spacing: 5px; font-weight: bold;'>" +
                token +
                "</div>" +
                "<p style='margin-top: 20px;'>Este código es válido por 5 minutos.</p>" +
                "<p>Si no solicitaste este código, puedes ignorar este correo.</p>" +
                "<p style='margin-top: 30px; font-size: 12px; color: #757575;'>Este es un correo automático, por favor no respondas a este mensaje.</p>" +
                "</div>";
    }

    /**
     * Logs message content as a backup in case email sending fails.
     *
     * @param to      Recipient email
     * @param subject Email subject
     * @param content Email content
     */
    private void logMessageContent(String to, String subject, String content) {
        logger.info("========================================");
        logger.info("EMAIL BACKUP - Could not send email");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("Content: {}", content);
        logger.info("========================================");
        
        // Also print to console for immediate visibility
        System.out.println("========================================");
        System.out.println("EMAIL BACKUP - Could not send email");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
        System.out.println("========================================");
    }
}
