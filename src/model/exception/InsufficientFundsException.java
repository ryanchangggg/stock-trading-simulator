package model.exception;

/**
 * Thrown when an account has insufficient funds to complete a financial operation.
 * <p>
 * This exception enforces the domain invariant that an account balance
 * must never go negative through withdrawals or trade settlements.
 */
public class InsufficientFundsException extends RuntimeException {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
