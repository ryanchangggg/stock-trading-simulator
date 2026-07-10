package model.order;

/**
 * Represents the lifecycle status of an order in the trading system.
 * <p>
 * An order starts as PENDING when first placed. It transitions to
 * FILLED when executed, or to CANCELLED if the user revokes it
 * before execution.
 */
public enum OrderStatus {
    /** Order has been submitted but not yet executed. */
    PENDING,
    
    /** Order has been fully executed. */
    FILLED,
    
    /** Order was revoked by the user before execution. */
    CANCELLED
}
