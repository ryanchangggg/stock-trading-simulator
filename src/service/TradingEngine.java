package service;

import strategy.LimitOrderExecutor;
import strategy.MarketOrderExecutor;
import strategy.OrderExecutionStrategy;
import strategy.StopLossExecutor;
import service.OrderValidator;
import model.exception.InvalidOrderException;
import model.market.Market;
import model.order.Order;
import model.order.OrderStatus;
import model.order.OrderType;
import model.user.User;
import repository.OrderRepository;
import repository.TradeRepository;
import repository.UserRepository;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Core orchestrator for all trading activity.
 * <p>
 * Validates orders, routes them to the appropriate execution strategy,
 * manages the pending-order book, processes pending orders on each
 * simulation tick, and persists all state changes through repository
 * interfaces. The engine catches execution exceptions as a safety net
 * and cancels failing orders gracefully.
 */
public class TradingEngine {

    static final long INITIAL_ORDER_ID = 1;
    static final long INITIAL_TRADE_ID = 1;

    private final OrderValidator validator;
    private final OrderBook orderBook;
    private final Map<OrderType, OrderExecutionStrategy> strategies;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;

    private long nextOrderId;
    private long nextTradeId;

    public TradingEngine(OrderRepository orderRepository,
                         TradeRepository tradeRepository,
                         UserRepository userRepository) {
        this.validator = new OrderValidator();
        this.orderBook = new OrderBook();
        this.strategies = new EnumMap<>(OrderType.class);
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.tradeRepository = Objects.requireNonNull(tradeRepository);
        this.userRepository = Objects.requireNonNull(userRepository);

        strategies.put(OrderType.MARKET, new MarketOrderExecutor());
        strategies.put(OrderType.LIMIT, new LimitOrderExecutor());
        strategies.put(OrderType.STOP_LOSS, new StopLossExecutor());

        this.nextOrderId = INITIAL_ORDER_ID;
        this.nextTradeId = INITIAL_TRADE_ID;
    }

    // ── ID Generation ─────────────────────────────────────

    /** Returns the next order ID and increments the counter. */
    public long nextOrderId() {
        return nextOrderId++;
    }

    /** Returns the next trade ID and increments the counter. */
    public long nextTradeId() {
        return nextTradeId++;
    }

    // ── Order Placement ───────────────────────────────────

    /**
     * Validates and executes (or queues) an order.
     * Market orders execute immediately; limit/stop-loss orders
     * are queued in the order book.
     */
    public ExecutionResult placeOrder(Order order, User user, Market market) {
        Objects.requireNonNull(order);
        Objects.requireNonNull(user);
        Objects.requireNonNull(market);

        try {
            validator.validate(order, user, market);
        } catch (InvalidOrderException e) {
            order.cancel();
            orderRepository.save(order);
            return ExecutionResult.rejected(order, e.getMessage());
        }

        OrderExecutionStrategy strategy = strategies.get(order.getOrderType());
        if (strategy == null) {
            throw new IllegalStateException(
                "No strategy for order type: " + order.getOrderType());
        }

        ExecutionResult result;
        try {
            result = strategy.execute(order, user, market, nextTradeId());
        } catch (Exception e) {
            order.cancel();
            orderRepository.save(order);
            return ExecutionResult.rejected(order, "Execution failed: " + e.getMessage());
        }

        persistExecutionResult(result, user);

        if (result.getFinalStatus() == OrderStatus.PENDING) {
            orderBook.add(order);
        }

        return result;
    }

    // ── Tick Processing ───────────────────────────────────

    /**
     * Processes all pending orders against current market prices.
     * Called on each simulation tick. Catches exceptions from
     * strategies and cancels failing orders gracefully.
     */
    public List<ExecutionResult> processPendingOrders(Market market) {
        Objects.requireNonNull(market);
        List<ExecutionResult> results = new ArrayList<>();

        for (Order order : orderBook.getAllPending()) {
            User user = userRepository.findById(order.getUserId()).orElse(null);
            if (user == null) {
                cancelOrphanedOrder(order);
                continue;
            }

            OrderExecutionStrategy strategy = strategies.get(order.getOrderType());
            if (strategy == null) continue;

            ExecutionResult result;
            try {
                result = strategy.execute(order, user, market, nextTradeId());
            } catch (Exception e) {
                cancelOrphanedOrder(order);
                continue;
            }

            if (result.getFinalStatus() == OrderStatus.FILLED) {
                orderBook.remove(order.getId());
                persistExecutionResult(result, user);
                results.add(result);
            }
        }

        return results;
    }

    // ── Order Cancellation ────────────────────────────────

    public boolean cancelOrder(long orderId, User user) {
        Objects.requireNonNull(user);
        Optional<Order> orderOpt = orderBook.findById(orderId);
        if (orderOpt.isEmpty()) return false;

        Order order = orderOpt.get();
        if (order.getUserId() != user.getId()) return false;
        if (order.getStatus() != OrderStatus.PENDING) return false;

        order.cancel();
        orderBook.remove(orderId);
        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED);
        return true;
    }

    // ── Queries ───────────────────────────────────────────

    public List<Order> getPendingOrders() {
        return orderBook.getAllPending();
    }

    public int getPendingOrderCount() {
        return orderBook.size();
    }

    public boolean hasPendingOrders() {
        return !orderBook.isEmpty();
    }

    // ── Private ───────────────────────────────────────────

    private void persistExecutionResult(ExecutionResult result, User user) {
        orderRepository.save(result.getOrder());
        result.getTrade().ifPresent(tradeRepository::save);
        userRepository.save(user);
    }

    private void cancelOrphanedOrder(Order order) {
        try {
            order.cancel();
        } catch (Exception ignored) {}
        orderBook.remove(order.getId());
        orderRepository.updateStatus(order.getId(), OrderStatus.CANCELLED);
    }
}
