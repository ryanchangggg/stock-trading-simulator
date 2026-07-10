package model.exception;

/**
 * Base exception for all domain-level errors in the trading system.
 * <p>
 * Provides a common superclass so callers can catch domain exceptions
 * broadly when needed, without depending on runtime exception details.
 */
public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
