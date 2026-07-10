package model.order;

/**
 * Enumerates the types of orders supported by the trading system.
 * <p>
 * MARKET  — executes immediately at the current market price.
 * LIMIT   — executes only at or better than a specified price.
 * STOP_LOSS — triggers a market order when the market price crosses
 *             a specified stop price.
 */
public enum OrderType {
    MARKET,
    LIMIT,
    STOP_LOSS
}
