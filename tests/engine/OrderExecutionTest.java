package service;

import model.exception.InsufficientFundsException;
import model.exception.InvalidOrderException;
import model.market.Market;
import model.order.*;
import model.portfolio.Portfolio;
import model.stock.EquityStock;
import model.trade.TradeType;
import model.user.User;
import repository.OrderRepository;
import repository.TradeRepository;
import repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the TradingEngine with mocked repositories.
 * Covers buy, sell, limit, stop-loss order execution and validation.
 */
@ExtendWith(MockitoExtension.class)
class OrderExecutionTest {

    @Mock private OrderRepository orderRepo;
    @Mock private TradeRepository tradeRepo;
    @Mock private UserRepository userRepo;

    private TradingEngine engine;
    private Market market;
    private User user;

    @BeforeEach
    void setUp() {
        engine = new TradingEngine(orderRepo, tradeRepo, userRepo);
        market = new Market();
        market.addStock(new EquityStock("AAPL", "Apple Inc.", "Technology",
            new BigDecimal("185.40"), 2_000_000_000L, 0));
        market.addStock(new EquityStock("GOOG", "Alphabet Inc.", "Technology",
            new BigDecimal("140.20"), 1_500_000_000L, 0));

        user = new User(1, "testuser", "hash", new BigDecimal("100000.00"));
        user.getPortfolio().addShares("AAPL", 50, new BigDecimal("180.00"));

        lenient().when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        lenient().when(orderRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(tradeRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // ── Market Buy ──────────────────────────────────────────

    @Test @DisplayName("Market buy order executes at current price")
    void marketBuy() {
        BuyOrder order = new BuyOrder(1, 1, "AAPL", 10,
            market.getCurrentPrice("AAPL"), OrderType.MARKET);
        ExecutionResult result = engine.placeOrder(order, user, market);
        assertTrue(result.isSuccess());
        assertTrue(result.getTrade().isPresent());
        assertEquals("AAPL", result.getTrade().get().getSymbol());
        // Cash should be reduced by 10 * 185.40 = $1,854
        assertEquals(0, new BigDecimal("98146.00").compareTo(user.getCashBalance()));
    }

    @Test @DisplayName("Market buy order reduces cash and adds shares")
    void marketBuyUpdatesPortfolio() {
        BuyOrder order = new BuyOrder(2, 1, "GOOG", 20,
            market.getCurrentPrice("GOOG"), OrderType.MARKET);
        engine.placeOrder(order, user, market);
        assertTrue(user.getPortfolio().holdsSymbol("GOOG"));
        // 20 * 140.20 = $2,804
        assertEquals(0, new BigDecimal("97196.00").compareTo(user.getCashBalance()));
    }

    // ── Market Sell ─────────────────────────────────────────

    @Test @DisplayName("Market sell order executes at current price")
    void marketSell() {
        SellOrder order = new SellOrder(3, 1, "AAPL", 20,
            market.getCurrentPrice("AAPL"), OrderType.MARKET);
        ExecutionResult result = engine.placeOrder(order, user, market);
        assertTrue(result.isSuccess());
        assertTrue(result.getTrade().isPresent());
        // 20 * 185.40 = $3,708 credited
        assertEquals(0, new BigDecimal("103708.00").compareTo(user.getCashBalance()));
    }

    @Test @DisplayName("Market sell removes shares from portfolio")
    void marketSellUpdatesPortfolio() {
        SellOrder order = new SellOrder(4, 1, "AAPL", 30,
            market.getCurrentPrice("AAPL"), OrderType.MARKET);
        engine.placeOrder(order, user, market);
        assertTrue(user.getPortfolio().holdsSymbol("AAPL"));
        assertEquals(20, user.getPortfolio().getPosition("AAPL").get().getQuantity());
    }

    @Test @DisplayName("Sell more than held throws InvalidOrderException")
    void sellTooMany() {
        SellOrder order = new SellOrder(5, 1, "AAPL", 999,
            market.getCurrentPrice("AAPL"), OrderType.MARKET);
        ExecutionResult result = engine.placeOrder(order, user, market);
        assertFalse(result.isSuccess());
    }

    // ── Limit Order ─────────────────────────────────────────

    @Test @DisplayName("Limit buy order is pending when price is above limit")
    void limitBuyPending() {
        // Current price is $185.40, limit is $180.00 → pending
        BuyOrder order = new BuyOrder(6, 1, "AAPL", 10,
            new BigDecimal("180.00"), OrderType.LIMIT);
        ExecutionResult result = engine.placeOrder(order, user, market);
        assertTrue(result.isSuccess());
        assertEquals(OrderStatus.PENDING, result.getFinalStatus());
        assertTrue(result.getTrade().isEmpty());
    }

    @Test @DisplayName("Limit buy fills when market price drops to limit")
    void limitBuyFillsOnTick() {
        // Place pending order at $180.00
        BuyOrder order = new BuyOrder(7, 1, "AAPL", 10,
            new BigDecimal("180.00"), OrderType.LIMIT);
        engine.placeOrder(order, user, market);

        // Market price drops to $180.00 → should fill
        market.updatePrice("AAPL", new BigDecimal("180.00"));
        var results = engine.processPendingOrders(market);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).isSuccess());
        assertEquals(OrderStatus.FILLED, results.get(0).getFinalStatus());
        assertTrue(results.get(0).getTrade().isPresent());
    }

    @Test @DisplayName("Limit sell fills when market price rises to limit")
    void limitSellFillsOnTick() {
        SellOrder order = new SellOrder(8, 1, "AAPL", 10,
            new BigDecimal("190.00"), OrderType.LIMIT);
        engine.placeOrder(order, user, market);

        market.updatePrice("AAPL", new BigDecimal("190.00"));
        var results = engine.processPendingOrders(market);
        assertFalse(results.isEmpty());
        assertEquals(OrderStatus.FILLED, results.get(0).getFinalStatus());
    }

    // ── Stop Loss ──────────────────────────────────────────

    @Test @DisplayName("Stop-loss sell triggers when price drops to stop")
    void stopLossTriggers() {
        StopLossOrder order = new StopLossOrder(9, 1, "AAPL", 20,
            new BigDecimal("170.00"), TradeType.SELL);
        engine.placeOrder(order, user, market);
        assertEquals(1, engine.getPendingOrderCount());

        // Price drops to $170.00 → trigger
        market.updatePrice("AAPL", new BigDecimal("170.00"));
        var results = engine.processPendingOrders(market);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).isSuccess());
    }

    @Test @DisplayName("Stop-loss does not trigger above stop price")
    void stopLossNotTriggered() {
        StopLossOrder order = new StopLossOrder(10, 1, "AAPL", 20,
            new BigDecimal("170.00"), TradeType.SELL);
        engine.placeOrder(order, user, market);

        // Price rises → no trigger
        market.updatePrice("AAPL", new BigDecimal("190.00"));
        var results = engine.processPendingOrders(market);
        assertTrue(results.isEmpty());
    }

    // ── Cash Balance Validation ─────────────────────────────

    @Test @DisplayName("Buy order exceeding cash balance is rejected")
    void buyOverBalanceRejected() {
        BuyOrder order = new BuyOrder(11, 1, "AAPL", 100000,
            market.getCurrentPrice("AAPL"), OrderType.MARKET);
        ExecutionResult result = engine.placeOrder(order, user, market);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("insufficient"));
    }

    @Test @DisplayName("Cash balance is unchanged after rejected order")
    void balanceUnchangedOnRejection() {
        BigDecimal before = user.getCashBalance();
        BuyOrder order = new BuyOrder(12, 1, "AAPL", 99999,
            market.getCurrentPrice("AAPL"), OrderType.MARKET);
        engine.placeOrder(order, user, market);
        assertEquals(0, before.compareTo(user.getCashBalance()));
    }

    // ── Cancel Order ───────────────────────────────────────

    @Test @DisplayName("Pending limit order can be cancelled")
    void cancelPendingOrder() {
        BuyOrder order = new BuyOrder(13, 1, "AAPL", 10,
            new BigDecimal("150.00"), OrderType.LIMIT);
        engine.placeOrder(order, user, market);
        assertTrue(engine.cancelOrder(13, user));
        assertEquals(0, engine.getPendingOrderCount());
    }

    @Test @DisplayName("Cannot cancel another user's order")
    void cancelOtherUserOrder() {
        User other = new User(2, "other", "hash", BigDecimal.ZERO);
        BuyOrder order = new BuyOrder(14, 2, "AAPL", 10,
            new BigDecimal("150.00"), OrderType.LIMIT);
        engine.placeOrder(order, other, market);
        assertFalse(engine.cancelOrder(14, user));
    }

    @Test @DisplayName("Filled order cannot be cancelled")
    void cancelFilledOrder() {
        SellOrder order = new SellOrder(15, 1, "AAPL", 10,
            market.getCurrentPrice("AAPL"), OrderType.MARKET);
        engine.placeOrder(order, user, market);
        assertFalse(engine.cancelOrder(15, user));
    }
}
