package strategy;

import service.ExecutionResult;
import model.market.Market;
import model.order.Order;
import model.user.User;

/**
 * Strategy interface for executing different types of orders.
 * <p>
 * Each order type (market, limit, stop-loss) has its own execution
 * semantics. Implementing this interface allows the TradingEngine
 * to handle new order types without modifying existing execution
 * logic, following the Open/Closed Principle.
 * <p>
 * Implementations are responsible for:
 * <ul>
 *   <li>Checking whether the order's conditions are met</li>
 *   <li>Executing the trade (deducting/crediting cash, adjusting portfolio)</li>
 *   <li>Creating the Trade record</li>
 *   <li>Updating the order's status</li>
 * </ul>
 */
public interface OrderExecutionStrategy {

    /**
     * Attempts to execute the given order.
     *
     * @param order  the order to execute
     * @param user   the user who placed the order (will be mutated)
     * @param market the current market state
     * @param nextTradeId the next available trade identifier
     * @return the result of the execution attempt
     */
    ExecutionResult execute(Order order, User user, Market market,
                            long nextTradeId);
}
