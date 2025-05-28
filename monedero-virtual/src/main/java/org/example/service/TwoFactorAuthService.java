package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling two-factor authentication.
 */
@Service
public class TwoFactorAuthService {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorAuthService.class);
    private static final int TOKEN_LENGTH = 6;
    private static final long TOKEN_VALIDITY_MINUTES = 5;

    // In a production environment, this should be replaced with a distributed cache like Hazelcast
    private final Map<String, TokenInfo> tokenStore = new HashMap<>();

    @Autowired
    private EmailService emailService;

    /**
     * Generates a verification token for the given username.
     *
     * @param username The username to generate a token for
     * @param email The email associated with the username (can be null)
     * @return The masked email for display
     */
    public String generateToken(String username, String email) {
        String token = generateRandomToken();
        tokenStore.put(username, new TokenInfo(token, System.currentTimeMillis()));

        // Create masked email for display
        String actualEmail = email != null ? email : username + "@example.com";
        String maskedEmail = maskEmail(actualEmail);

        // Send token via email
        try {
            emailService.sendVerificationToken(actualEmail, token);
        } catch (Exception e) {
            // If email sending fails, log the error but continue
            // The token will still be printed to the console as a backup
            logger.error("Failed to send verification token email to: {}", actualEmail, e);
        }

        // Always log to console as a backup
        logger.info("2FA Token for user {}: {}", username, token);
        System.out.println("========================================");
        System.out.println("2FA TOKEN for " + maskedEmail + ": " + token);
        System.out.println("========================================");

        return maskedEmail;
    }

    /**
     * Masks an email address for privacy.
     *
     * @param email The email to mask
     * @return The masked email
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return "****@example.com";
        }

        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        // Keep first character and last character, mask the rest
        String maskedName = name.length() <= 2
            ? name.charAt(0) + "***"
            : name.charAt(0) + "***" + name.charAt(name.length() - 1);

        return maskedName + "@" + domain;
    }

    /**
     * Validates a token for the given username.
     *
     * @param username The username to validate the token for
     * @param token The token to validate
     * @return True if the token is valid, false otherwise
     */
    public boolean validateToken(String username, String token) {
        TokenInfo storedToken = tokenStore.get(username);

        if (storedToken == null) {
            return false;
        }

        // Check if token has expired
        long currentTime = System.currentTimeMillis();
        long tokenAge = currentTime - storedToken.timestamp;
        if (tokenAge > TimeUnit.MINUTES.toMillis(TOKEN_VALIDITY_MINUTES)) {
            tokenStore.remove(username);
            return false;
        }

        // Check if token matches
        boolean isValid = storedToken.token.equals(token);

        // Remove token after validation (one-time use)
        if (isValid) {
            tokenStore.remove(username);
        }

        return isValid;
    }

    /**
     * Generates a random numeric token.
     *
     * @return A random token
     */
    private String generateRandomToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    /**
     * Inner class to store token information.
     */
    private static class TokenInfo {
        private final String token;
        private final long timestamp;

        public TokenInfo(String token, long timestamp) {
            this.token = token;
            this.timestamp = timestamp;
        }
    }
}
