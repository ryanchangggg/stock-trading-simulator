package model.order;

import model.exception.InvalidOrderException;
import model.trade.TradeType;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A stop-loss order that triggers a market order when the market price
 * crosses a specified threshold.
 * <p>
 * For a sell stop-loss, the order triggers when the market price falls
 * to or below the stop price. For a buy stop-loss, the order triggers
 * when the market price rises to or above the stop price.
 * <p>
 * Once triggered, a stop-loss order behaves as a market order and
 * executes at the prevailing market price.
 */
public class StopLossOrder extends Order {

    private final BigDecimal stopPrice;
    private boolean triggered;

    /**
     * Constructs a StopLossOrder.
     *
     * @param id        the unique order identifier
     * @param userId    the identifier of the user placing the order
     * @param symbol    the stock ticker symbol
     * @param quantity  the number of shares; must be positive
     * @param stopPrice the price at which the order triggers; must be positive
     * @param tradeType BUY or SELL
     * @throws NullPointerException     if stopPrice is null
     * @throws IllegalArgumentException if stopPrice is not positive
     */
    public StopLossOrder(long id, long userId, String symbol, int quantity,
                         BigDecimal stopPrice, TradeType tradeType) {
        super(id, userId, symbol, quantity, BigDecimal.ZERO,
              OrderType.STOP_LOSS, tradeType);
        this.stopPrice = Objects.requireNonNull(stopPrice,
            "Stop price must not be null");
        if (stopPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Stop price must be positive");
        }
        this.triggered = false;
    }

    /**
     * Returns the stop price at which this order triggers.
     */
    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    /**
     * Returns true if this order has been triggered by the market price
     * crossing the stop threshold.
     */
    public boolean isTriggered() {
        return triggered;
    }

    /**
     * Marks this order as triggered. Once triggered, it will be
     * executed as a market order.
     *
     * @throws IllegalStateException if the order is not in PENDING status
     */
    public void trigger() {
        if (getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot trigger an order with status: " + getStatus());
        }
        this.triggered = true;
    }

    /**
     * Checks whether the current market price should trigger this order.
     * <p>
     * For a SELL stop-loss, triggers when market price falls to or below
     * the stop price. For a BUY stop-loss, triggers when market price
     * rises to or above the stop price.
     *
     * @param marketPrice the current market price
     * @return true if the order should trigger
     */
    public boolean shouldTrigger(BigDecimal marketPrice) {
        Objects.requireNonNull(marketPrice, "Market price must not be null");
        if (getTradeType() == TradeType.SELL) {
            return marketPrice.compareTo(stopPrice) <= 0;
        } else {
            return marketPrice.compareTo(stopPrice) >= 0;
        }
    }

    /**
     * Validates stop-loss specific rules.
     * <p>
     * Ensures the stop price is positive and sensible.
     *
     * @throws InvalidOrderException if validation fails
     */
    @Override
    public void validate() {
        if (stopPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException(
                "Stop-loss order must have a positive stop price");
        }
    }

    @Override
    public String getDescription() {
        return "Stop-Loss " + getTradeType();
    }

    @Override
    public String toString() {
        return getDescription() + "{id=" + getId()
            + ", symbol='" + getSymbol() + '\''
            + ", quantity=" + getQuantity()
            + ", stopPrice=" + stopPrice
            + ", triggered=" + triggered
            + ", status=" + getStatus() + '}';
    }
}
