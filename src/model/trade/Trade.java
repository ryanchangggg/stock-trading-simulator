package model.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a completed trade in the trading system.
 * <p>
 * A Trade is created when an order is filled. It records the final
 * execution details: which user traded, which security, the direction
 * (buy/sell), quantity, execution price, total value, and timestamps.
 * Trades are immutable once created and form the audit trail for
 * all market activity.
 */
public class Trade {

    private final long id;
    private final long userId;
    private final String symbol;
    private final TradeType tradeType;
    private final int quantity;
    private final BigDecimal price;
    private final BigDecimal totalValue;
    private final LocalDateTime tradeDate;
    private final LocalDateTime timestamp;

    /**
     * Constructs a Trade with the specified attributes.
     *
     * @param id        the unique trade identifier
     * @param userId    the identifier of the user who executed the trade
     * @param symbol    the stock ticker symbol
     * @param tradeType BUY or SELL
     * @param quantity  the number of shares traded; must be positive
     * @param price     the execution price per share; must be non-negative
     * @throws NullPointerException     if symbol or tradeType is null
     * @throws IllegalArgumentException if quantity is not positive,
     *                                  or price is negative
     */
    public Trade(long id, long userId, String symbol, TradeType tradeType,
                 int quantity, BigDecimal price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Trade quantity must be positive");
        }
        this.id = id;
        this.userId = userId;
        this.symbol = Objects.requireNonNull(symbol, "Symbol must not be null");
        this.tradeType = Objects.requireNonNull(tradeType,
            "Trade type must not be null");
        this.quantity = quantity;
        this.price = Objects.requireNonNull(price, "Price must not be null");
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.totalValue = price.multiply(BigDecimal.valueOf(quantity));
        this.tradeDate = LocalDateTime.now();
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Returns the unique trade identifier.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the user identifier who executed this trade.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Returns the stock ticker symbol.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the direction of the trade (BUY or SELL).
     */
    public TradeType getTradeType() {
        return tradeType;
    }

    /**
     * Returns the trade type name (BUY or SELL).
     */
    public String getTradeTypeName() {
        return tradeType.name();
    }

    /**
     * Returns the number of shares traded.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the execution price per share.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Returns the total value of the trade (price * quantity).
     */
    public BigDecimal getTotalValue() {
        return totalValue;
    }

    /**
     * Returns the date on which the trade occurred.
     */
    public LocalDateTime getTradeDate() {
        return tradeDate;
    }

    /**
     * Returns the timestamp when the trade was recorded.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Trade{id=" + id
            + ", userId=" + userId
            + ", symbol='" + symbol + '\''
            + ", type=" + tradeType
            + ", quantity=" + quantity
            + ", price=" + price
            + ", total=" + totalValue + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return id == trade.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
