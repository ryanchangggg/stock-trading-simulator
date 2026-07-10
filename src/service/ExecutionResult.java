package service;

import model.order.Order;
import model.order.OrderStatus;
import model.trade.Trade;
import java.util.Optional;

/**
 * Encapsulates the outcome of an order execution attempt.
 * <p>
 * Carries the success/failure status, a human-readable message,
 * the resulting Trade if the order was filled, and the final
 * status of the order. Designed to give callers complete
 * information about what happened without needing to inspect
 * the engine's internal state.
 */
public class ExecutionResult {

    private final boolean success;
    private final String message;
    private final Trade trade;
    private final Order order;
    private final OrderStatus finalStatus;

    private ExecutionResult(boolean success, String message,
                            Trade trade, Order order,
                            OrderStatus finalStatus) {
        this.success = success;
        this.message = message;
        this.trade = trade;
        this.order = order;
        this.finalStatus = finalStatus;
    }

    /**
     * Creates a successful execution result for a filled order.
     */
    public static ExecutionResult filled(Order order, Trade trade) {
        return new ExecutionResult(true,
            "Order filled: " + order.getDescription(),
            trade, order, OrderStatus.FILLED);
    }

    /**
     * Creates a successful result for a pending order (limit or stop-loss
     * that has been accepted and is waiting for price conditions).
     */
    public static ExecutionResult pending(Order order) {
        return new ExecutionResult(true,
            "Order placed and pending: " + order.getDescription(),
            null, order, OrderStatus.PENDING);
    }

    /**
     * Creates a failed execution result.
     */
    public static ExecutionResult rejected(Order order, String reason) {
        return new ExecutionResult(false,
            "Order rejected: " + reason,
            null, order, OrderStatus.CANCELLED);
    }

    /**
     * Creates a result for a cancelled order.
     */
    public static ExecutionResult cancelled(Order order) {
        return new ExecutionResult(true,
            "Order cancelled: " + order.getDescription(),
            null, order, OrderStatus.CANCELLED);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    /**
     * Returns the trade if the order was filled, or empty otherwise.
     */
    public Optional<Trade> getTrade() { return Optional.ofNullable(trade); }
    public Order getOrder() { return order; }
    public OrderStatus getFinalStatus() { return finalStatus; }

    @Override
    public String toString() {
        return (success ? "SUCCESS" : "FAILURE")
            + ": " + message
            + (trade != null ? " | trade=" + trade : "");
    }
}
