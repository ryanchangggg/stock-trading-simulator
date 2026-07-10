package model.exception;

/**
 * Thrown when an order violates business rules.
 * <p>
 * Examples include: attempting to sell more shares than held,
 * placing a buy order that exceeds available cash, or specifying
 * a non-positive quantity.
 */
public class InvalidOrderException extends RuntimeException {
    
    public InvalidOrderException(String message) {
        super(message);
    }
    
    public InvalidOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
