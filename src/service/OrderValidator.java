package service;

import model.exception.InvalidOrderException;
import model.market.Market;
import model.order.Order;
import model.order.StopLossOrder;
import model.trade.TradeType;
import model.user.User;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Validates orders against business rules before execution.
 * <p>
 * Performs two categories of validation:
 * <ul>
 *   <li><b>Structural validation</b> — calls {@link Order#validate()} to
 *       enforce order-type specific rules (e.g., limit price must be positive).</li>
 *   <li><b>Precondition validation</b> — checks that the user has sufficient
 *       cash (for buys) or shares (for sells) to execute the order.</li>
 * </ul>
 * Validation failures throw {@link InvalidOrderException} with a
 * descriptive message.
 */
public class OrderValidator {

    /**
     * Validates an order against business rules.
     *
     * @param order  the order to validate
     * @param user   the user placing the order
     * @param market the current market state
     * @throws InvalidOrderException if any validation rule is violated
     */
    public void validate(Order order, User user, Market market) {
        Objects.requireNonNull(order, "Order must not be null");
        Objects.requireNonNull(user, "User must not be null");
        Objects.requireNonNull(market, "Market must not be null");

        // 1. Order-level structural validation
        order.validate();

        // 2. Market price availability
        String symbol = order.getSymbol();
        if (!market.getStock(symbol).isPresent()) {
            throw new InvalidOrderException(
                "Stock not found in market: " + symbol);
        }

        // 3. Financial preconditions
        if (order.getTradeType() == TradeType.BUY) {
            validateBuyOrder(order, user, market);
        } else {
            validateSellOrder(order, user);
        }
    }

    private void validateBuyOrder(Order order, User user, Market market) {
        BigDecimal currentPrice = market.getCurrentPrice(order.getSymbol());
        BigDecimal totalCost = currentPrice.multiply(
            BigDecimal.valueOf(order.getQuantity()));

        if (!user.canAfford(totalCost)) {
            throw new InvalidOrderException(
                "Insufficient funds: cash=" + user.getCashBalance()
                    + ", required=" + totalCost);
        }
    }

    private void validateSellOrder(Order order, User user) {
        if (!user.holdsAtLeast(order.getSymbol(), order.getQuantity())) {
            throw new InvalidOrderException(
                "Insufficient shares: symbol=" + order.getSymbol()
                    + ", requested=" + order.getQuantity());
        }
    }
}
