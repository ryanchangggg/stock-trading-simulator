package service;
import model.market.Market;
import model.order.*;
import model.trade.Trade;
import model.trade.TradeType;
import model.user.User;
import repository.OrderRepository;
import repository.TradeRepository;
import repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;

/**
 * High-level trading operations for the UI layer.
 * <p>
 * Wraps the {@link TradingEngine} and repository interfaces,
 * translating engine-level {@link ExecutionResult} objects into
 * service-level {@link Result} objects. Order IDs are sourced from
 * the engine to ensure consistency across the system.
 */
public class TradingService {

    private final TradingEngine engine;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final Market market;

    public TradingService(TradingEngine engine, OrderRepository orderRepo,
                          TradeRepository tradeRepo, UserRepository userRepo,
                          Market market) {
        this.engine = engine;
        this.orderRepository = orderRepo;
        this.tradeRepository = tradeRepo;
        this.userRepository = userRepo;
        this.market = market;
    }

    // ── Order placement ───────────────────────────────────

    public Result<Trade> buyMarket(Session session, String symbol, int qty) {
        return executeMarket(session, symbol, qty, TradeType.BUY);
    }

    public Result<Trade> sellMarket(Session session, String symbol, int qty) {
        return executeMarket(session, symbol, qty, TradeType.SELL);
    }

    public Result<Order> buyLimit(Session session, String symbol,
                                  int qty, BigDecimal limit) {
        return placePending(session, new BuyOrder(
            engine.nextOrderId(), session.getUserId(), symbol,
            qty, limit, OrderType.LIMIT));
    }

    public Result<Order> sellLimit(Session session, String symbol,
                                   int qty, BigDecimal limit) {
        return placePending(session, new SellOrder(
            engine.nextOrderId(), session.getUserId(), symbol,
            qty, limit, OrderType.LIMIT));
    }

    public Result<Order> buyStopLoss(Session session, String symbol,
                                     int qty, BigDecimal stop) {
        return placePending(session, new StopLossOrder(
            engine.nextOrderId(), session.getUserId(), symbol,
            qty, stop, TradeType.BUY));
    }

    public Result<Order> sellStopLoss(Session session, String symbol,
                                      int qty, BigDecimal stop) {
        return placePending(session, new StopLossOrder(
            engine.nextOrderId(), session.getUserId(), symbol,
            qty, stop, TradeType.SELL));
    }

    // ── Order management ──────────────────────────────────

    public Result<Void> cancelOrder(Session session, long orderId) {
        User user = userRepository.findById(session.getUserId()).orElse(null);
        if (user == null) return Result.failure("User not found");
        boolean ok = engine.cancelOrder(orderId, user);
        return ok ? Result.success()
                  : Result.failure("Order not found or cannot be cancelled");
    }

    // ── Queries ───────────────────────────────────────────

    public Result<List<Order>> getOrders(Session session) {
        return Result.success(
            orderRepository.findByUserId(session.getUserId()));
    }

    public Result<List<Order>> getPendingOrders(Session session) {
        return Result.success(
            orderRepository.findByStatus(OrderStatus.PENDING));
    }

    public Result<List<Trade>> getTradeHistory(Session session) {
        return Result.success(
            tradeRepository.findByUserId(session.getUserId()));
    }

    // ── Internals ─────────────────────────────────────────

    private Result<Trade> executeMarket(Session session, String symbol,
                                        int qty, TradeType type) {
        User user = userRepository.findById(session.getUserId()).orElse(null);
        if (user == null) return Result.failure("User not found");

        Order order = (type == TradeType.BUY)
            ? new BuyOrder(engine.nextOrderId(), user.getId(), symbol, qty,
                market.getCurrentPrice(symbol), OrderType.MARKET)
            : new SellOrder(engine.nextOrderId(), user.getId(), symbol, qty,
                market.getCurrentPrice(symbol), OrderType.MARKET);

        ExecutionResult result = engine.placeOrder(order, user, market);
        if (!result.isSuccess())
            return Result.failure(result.getMessage());

        return result.getTrade()
            .map(Result::success)
            .orElse(Result.failure("Order was not executed"));
    }

    private Result<Order> placePending(Session session, Order order) {
        User user = userRepository.findById(session.getUserId()).orElse(null);
        if (user == null) return Result.failure("User not found");

        ExecutionResult result = engine.placeOrder(order, user, market);
        if (!result.isSuccess())
            return Result.failure(result.getMessage());

        return Result.success(order);
    }
}
