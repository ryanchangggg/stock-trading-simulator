package strategy;

import service.ExecutionResult;
import model.exception.InsufficientFundsException;
import model.exception.InvalidOrderException;
import model.market.Market;
import model.order.Order;
import model.order.OrderType;
import model.order.StopLossOrder;
import model.trade.Trade;
import model.trade.TradeType;
import model.user.User;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Handles stop-loss order lifecycle and trigger-based execution.
 * <p>
 * When placed, a stop-loss order sits pending until the market price
 * crosses the stop threshold. Once triggered, executes immediately
 * at the prevailing market price. Re-validates user state at execution
 * time since conditions may have changed since placement.
 */
public class StopLossExecutor implements OrderExecutionStrategy {

    @Override
    public ExecutionResult execute(Order order, User user, Market market,
                                   long nextTradeId) {
        Objects.requireNonNull(order, "Order must not be null");
        Objects.requireNonNull(user, "User must not be null");
        Objects.requireNonNull(market, "Market must not be null");

        if (!(order instanceof StopLossOrder)) {
            throw new IllegalArgumentException(
                "StopLossExecutor requires a StopLossOrder, got: "
                    + order.getClass().getSimpleName());
        }

        StopLossOrder stopOrder = (StopLossOrder) order;
        String symbol = stopOrder.getSymbol();
        BigDecimal currentPrice = market.getCurrentPrice(symbol);

        // Check if the stop-loss has been triggered
        if (!stopOrder.isTriggered()) {
            if (!stopOrder.shouldTrigger(currentPrice)) {
                return ExecutionResult.pending(stopOrder);
            }
            stopOrder.trigger();
        }

        // Re-validate: state may have changed since order was placed
        int quantity = stopOrder.getQuantity();
        BigDecimal fillPrice = currentPrice;

        try {
            if (stopOrder.getTradeType() == TradeType.SELL) {
                user.getPortfolio().removeShares(symbol, quantity);
                user.deposit(fillPrice.multiply(BigDecimal.valueOf(quantity)));
            } else {
                user.withdraw(fillPrice.multiply(BigDecimal.valueOf(quantity)));
                user.getPortfolio().addShares(symbol, quantity, fillPrice);
            }
        } catch (InsufficientFundsException | InvalidOrderException e) {
            stopOrder.cancel();
            return ExecutionResult.rejected(stopOrder,
                "Stop-loss cancelled: " + e.getMessage());
        }

        stopOrder.fill();
        Trade trade = new Trade(nextTradeId, user.getId(), symbol,
                                stopOrder.getTradeType(), quantity, fillPrice);
        return ExecutionResult.filled(stopOrder, trade);
    }
}
