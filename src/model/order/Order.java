package model.order;

import model.exception.InvalidOrderException;
import model.trade.TradeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a user's intent to buy or sell a security.
 * <p>
 * Serves as the abstract base class for all order types.
 * Each order carries the user identifier, stock symbol, quantity,
 * price, direction (buy/sell), and lifecycle status.
 * Subclasses define specific execution semantics (market vs. limit).
 * <p>
 * Orders enforce domain invariants: price must be non-negative,
 * quantity must be positive, and the order must reference a known
 * user and symbol.
 */
public abstract class Order {

    private final long id;
    private final long userId;
    private final String symbol;
    private final int quantity;
    private final BigDecimal price;
    private final OrderType orderType;
    private final TradeType tradeType;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime filledAt;

    /**
     * Constructs an Order with the specified attributes.
     *
     * @param id        the unique order identifier
     * @param userId    the identifier of the user placing the order
     * @param symbol    the stock ticker symbol
     * @param quantity  the number of shares; must be positive
     * @param price     the price per share; must be non-negative
     * @param orderType MARKET or LIMIT
     * @param tradeType BUY or SELL
     * @throws NullPointerException     if orderType or tradeType is null
     * @throws IllegalArgumentException if quantity is not positive,
     *                                  or price is negative
     */
    protected Order(long id, long userId, String symbol, int quantity,
                    BigDecimal price, OrderType orderType, TradeType tradeType) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Order quantity must be positive");
        }
        this.id = id;
        this.userId = userId;
        this.symbol = Objects.requireNonNull(symbol, "Symbol must not be null");
        this.quantity = quantity;
        this.price = Objects.requireNonNull(price, "Price must not be null");
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.orderType = Objects.requireNonNull(orderType,
            "Order type must not be null");
        this.tradeType = Objects.requireNonNull(tradeType,
            "Trade type must not be null");
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Returns the unique order identifier.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the user identifier who placed this order.
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
     * Returns the number of shares in this order.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the price per share for this order.
     * For MARKET orders, this is the expected execution price.
     * For LIMIT orders, this is the limit price.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Returns the total value of this order (price * quantity).
     */
    public BigDecimal getTotalValue() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Returns the type of this order (MARKET or LIMIT).
     */
    public OrderType getOrderType() {
        return orderType;
    }

    /**
     * Returns the trade direction (BUY or SELL).
     */
    public TradeType getTradeType() {
        return tradeType;
    }

    /**
     * Returns the current lifecycle status of this order.
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Returns the creation timestamp.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the timestamp when the order was filled, or null if not filled.
     */
    public LocalDateTime getFilledAt() {
        return filledAt;
    }

    /**
     * Marks this order as filled and records the fill timestamp.
     *
     * @throws IllegalStateException if the order is not in PENDING status
     */
    public void fill() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot fill an order with status: " + this.status);
        }
        this.status = OrderStatus.FILLED;
        this.filledAt = LocalDateTime.now();
    }

    /**
     * Cancels this order.
     *
     * @throws IllegalStateException if the order is not in PENDING status
     */
    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot cancel an order with status: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Directly restores the order status and fill timestamp from persistence.
     * <p>
     * This method bypasses the state machine guards in {@link #fill()}
     * and {@link #cancel()} because it is used exclusively when
     * reconstructing orders from the database, where the status is
     * already authoritative. Not intended for general use.
     *
     * @param status   the status to restore
     * @param filledAt the fill timestamp, or null if not filled
     */
    public void restoreStatus(OrderStatus status, LocalDateTime filledAt) {
        this.status = status;
        this.filledAt = filledAt;
    }

    /**
     * Validates that this order can be executed.
     * <p>
     * Subclasses provide type-specific validation logic.
     * For example, limit orders validate that the limit price is sensible.
     *
     * @throws InvalidOrderException if the order violates business rules
     */
    public abstract void validate();

    /**
     * Returns a human-readable description of this order type.
     *
     * @return e.g., "Market Buy" or "Limit Sell"
     */
    public abstract String getDescription();

    @Override
    public String toString() {
        return getDescription() + "{id=" + id
            + ", symbol='" + symbol + '\''
            + ", quantity=" + quantity
            + ", price=" + price
            + ", status=" + status + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
