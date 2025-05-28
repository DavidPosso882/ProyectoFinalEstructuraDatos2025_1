package org.example.service;

/**
 * Interface for email service operations.
 */
public interface EmailService {

    /**
     * Sends a simple text email.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param text    Email body text
     */
    void sendSimpleMessage(String to, String subject, String text);

    /**
     * Sends an HTML email.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param html    Email body in HTML format
     */
    void sendHtmlMessage(String to, String subject, String html);

    /**
     * Sends a verification token email.
     *
     * @param to    Recipient email address
     * @param token Verification token
     */
    void sendVerificationToken(String to, String token);
}
