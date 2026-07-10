package model.order;

import model.exception.InvalidOrderException;
import model.trade.TradeType;
import java.math.BigDecimal;

/**
 * Represents a user's intent to sell a security they already hold.
 * <p>
 * SellOrder extends Order with sell-specific validation rules.
 * It can be created as either a MARKET or LIMIT sell order.
 * Market sell orders execute immediately at the current price;
 * limit sell orders execute only at or above the specified limit price.
 */
public class SellOrder extends Order {

    /**
     * Constructs a SellOrder with the specified attributes.
     *
     * @param id        the unique order identifier
     * @param userId    the identifier of the user placing the order
     * @param symbol    the stock ticker symbol to sell
     * @param quantity  the number of shares to sell; must be positive
     * @param price     the price per share (execution price for MARKET,
     *                  limit price for LIMIT); must be non-negative
     * @param orderType MARKET or LIMIT
     */
    public SellOrder(long id, long userId, String symbol, int quantity,
                     BigDecimal price, OrderType orderType) {
        super(id, userId, symbol, quantity, price, orderType, TradeType.SELL);
    }

    /**
     * Validates sell-specific business rules.
     * <p>
     * For a LIMIT sell, ensures the limit price is positive
     * (a zero or negative limit price is not actionable).
     *
     * @throws InvalidOrderException if validation fails
     */
    @Override
    public void validate() {
        if (getOrderType() == OrderType.LIMIT
            && getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException(
                "Limit sell order must have a positive limit price");
        }
    }

    /**
     * Returns a human-readable description of this order.
     */
    @Override
    public String getDescription() {
        return (getOrderType() == OrderType.MARKET ? "Market" : "Limit")
            + " Sell";
    }
}
