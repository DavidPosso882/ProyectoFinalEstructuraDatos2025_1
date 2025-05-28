package org.example.exception;

/**
 * Custom exception for business logic errors.
 * This exception should be thrown when a business rule is violated.
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
