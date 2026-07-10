package strategy;

import service.ExecutionResult;
import model.exception.InsufficientFundsException;
import model.exception.InvalidOrderException;
import model.market.Market;
import model.order.Order;
import model.order.OrderType;
import model.trade.Trade;
import model.trade.TradeType;
import model.user.User;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Handles the lifecycle of limit orders.
 * <p>
 * On each tick, checks whether the market price meets the limit condition.
 * Re-validates user state at execution time since conditions may have
 * changed since the order was placed.
 */
public class LimitOrderExecutor implements OrderExecutionStrategy {

    @Override
    public ExecutionResult execute(Order order, User user, Market market,
                                   long nextTradeId) {
        Objects.requireNonNull(order, "Order must not be null");
        Objects.requireNonNull(user, "User must not be null");
        Objects.requireNonNull(market, "Market must not be null");

        String symbol = order.getSymbol();
        BigDecimal currentPrice = market.getCurrentPrice(symbol);
        BigDecimal limitPrice = order.getPrice();

        if (!canFillAtPrice(order, currentPrice, limitPrice)) {
            return ExecutionResult.pending(order);
        }

        int quantity = order.getQuantity();
        BigDecimal fillPrice = currentPrice;

        try {
            if (order.getTradeType() == TradeType.BUY) {
                user.withdraw(fillPrice.multiply(BigDecimal.valueOf(quantity)));
                user.getPortfolio().addShares(symbol, quantity, fillPrice);
            } else {
                user.getPortfolio().removeShares(symbol, quantity);
                user.deposit(fillPrice.multiply(BigDecimal.valueOf(quantity)));
            }
        } catch (InsufficientFundsException | InvalidOrderException e) {
            order.cancel();
            return ExecutionResult.rejected(order,
                "Limit order cancelled: " + e.getMessage());
        }

        order.fill();
        Trade trade = new Trade(nextTradeId, user.getId(), symbol,
                                order.getTradeType(), quantity, fillPrice);
        return ExecutionResult.filled(order, trade);
    }

    private boolean canFillAtPrice(Order order, BigDecimal marketPrice,
                                   BigDecimal limitPrice) {
        if (order.getTradeType() == TradeType.BUY) {
            return marketPrice.compareTo(limitPrice) <= 0;
        } else {
            return marketPrice.compareTo(limitPrice) >= 0;
        }
    }
}
