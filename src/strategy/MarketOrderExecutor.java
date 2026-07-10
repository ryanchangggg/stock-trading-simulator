package strategy;

import service.ExecutionResult;
import model.market.Market;
import model.order.Order;
import model.order.OrderType;
import model.trade.Trade;
import model.trade.TradeType;
import model.user.User;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Executes market orders immediately at the current market price.
 * <p>
 * A market buy order deducts the total cost from the user's cash
 * balance and adds shares to their portfolio. A market sell order
 * removes shares from the portfolio and credits the cash balance.
 * Both record a completed Trade in the audit trail.
 * <p>
 * This executor only handles orders of type {@link OrderType#MARKET}.
 */
public class MarketOrderExecutor implements OrderExecutionStrategy {

    @Override
    public ExecutionResult execute(Order order, User user, Market market,
                                   long nextTradeId) {
        Objects.requireNonNull(order, "Order must not be null");
        Objects.requireNonNull(user, "User must not be null");
        Objects.requireNonNull(market, "Market must not be null");

        String symbol = order.getSymbol();
        int quantity = order.getQuantity();
        BigDecimal currentPrice = market.getCurrentPrice(symbol);
        BigDecimal totalValue = currentPrice.multiply(
            BigDecimal.valueOf(quantity));

        if (order.getTradeType() == TradeType.BUY) {
            return executeBuy(order, user, symbol, quantity,
                              currentPrice, totalValue, nextTradeId);
        } else {
            return executeSell(order, user, symbol, quantity,
                               currentPrice, totalValue, nextTradeId);
        }
    }

    private ExecutionResult executeBuy(Order order, User user,
                                       String symbol, int quantity,
                                       BigDecimal price, BigDecimal total,
                                       long tradeId) {
        // Deduct cash
        user.withdraw(total);
        // Add shares to portfolio (average cost recalculated internally)
        user.getPortfolio().addShares(symbol, quantity, price);
        // Mark order as filled
        order.fill();
        // Create trade record
        Trade trade = new Trade(tradeId, user.getId(), symbol,
                                TradeType.BUY, quantity, price);
        return ExecutionResult.filled(order, trade);
    }

    private ExecutionResult executeSell(Order order, User user,
                                        String symbol, int quantity,
                                        BigDecimal price, BigDecimal total,
                                        long tradeId) {
        // Remove shares from portfolio
        user.getPortfolio().removeShares(symbol, quantity);
        // Credit cash
        user.deposit(total);
        // Mark order as filled
        order.fill();
        // Create trade record
        Trade trade = new Trade(tradeId, user.getId(), symbol,
                                TradeType.SELL, quantity, price);
        return ExecutionResult.filled(order, trade);
    }
}
