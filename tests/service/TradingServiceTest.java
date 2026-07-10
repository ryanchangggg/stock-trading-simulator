package service;

import service.ExecutionResult;
import service.TradingEngine;
import model.market.Market;
import model.order.BuyOrder;
import model.order.Order;
import model.stock.EquityStock;
import model.trade.Trade;
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
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the TradingService layer with mocked engine and repositories.
 */
@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock private TradingEngine engine;
    @Mock private Order order;
    @Mock private OrderRepository orderRepo;
    @Mock private TradeRepository tradeRepo;
    @Mock private UserRepository userRepo;

    private TradingService service;
    private Market market;
    private Session session;
    private User user;

    @BeforeEach
    void setUp() {
        market = new Market();
        market.addStock(new EquityStock("AAPL", "Apple Inc.", "Technology",
            new BigDecimal("185.40"), 2_000_000_000L, 0));

        user = new User(1, "trader", "hash", new BigDecimal("100000.00"));
        user.getPortfolio().addShares("AAPL", 50, new BigDecimal("180.00"));
        session = new Session(user);

        service = new TradingService(engine, orderRepo, tradeRepo, userRepo, market);

        lenient().when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        lenient().when(orderRepo.findByUserId(1L)).thenReturn(List.of());
        lenient().when(tradeRepo.findByUserId(1L)).thenReturn(List.of());
    }

    @Test @DisplayName("buyMarket returns trade when engine succeeds")
    void buyMarketSuccess() {
        Trade trade = new Trade(1, 1, "AAPL", TradeType.BUY, 10, new BigDecimal("185.40"));
        doReturn(ExecutionResult.filled(order, trade))
            .when(engine).placeOrder(any(), any(), any());

        Result<Trade> result = service.buyMarket(session, "AAPL", 10);
        assertTrue(result.isSuccess());
        assertEquals("AAPL", result.getValue().getSymbol());
    }

    @Test @DisplayName("buyMarket returns failure when engine rejects")
    void buyMarketFailure() {
        when(engine.placeOrder(any(), any(), any()))
            .thenReturn(ExecutionResult.rejected(
                mock(Order.class), "Insufficient funds"));
        Result<Trade> result = service.buyMarket(session, "AAPL", 99999);
        assertTrue(result.isFailure());
    }

    @Test @DisplayName("sellMarket returns trade when engine succeeds")
    void sellMarketSuccess() {
        Trade trade = new Trade(2, 1, "AAPL", TradeType.SELL, 10, new BigDecimal("185.40"));
        doReturn(ExecutionResult.filled(order, trade))
            .when(engine).placeOrder(any(), any(), any());
        Result<Trade> result = service.sellMarket(session, "AAPL", 10);
        assertTrue(result.isSuccess());
    }

    @Test @DisplayName("buyLimit returns pending order")
    void buyLimitPending() {
        doReturn(ExecutionResult.pending(order))
            .when(engine).placeOrder(any(), any(), any());
        Result<Order> result = service.buyLimit(session, "AAPL", 10, new BigDecimal("180.00"));
        assertTrue(result.isSuccess());
    }

    @Test @DisplayName("sellStopLoss places stop-loss order")
    void sellStopLoss() {
        doReturn(ExecutionResult.pending(order))
            .when(engine).placeOrder(any(), any(), any());
        Result<Order> result = service.sellStopLoss(session, "AAPL", 10, new BigDecimal("170.00"));
        assertTrue(result.isSuccess());
    }

    @Test @DisplayName("cancelOrder returns true when engine succeeds")
    void cancelOrder() {
        when(engine.cancelOrder(anyLong(), any())).thenReturn(true);
        Result<Void> result = service.cancelOrder(session, 1L);
        assertTrue(result.isSuccess());
    }

    @Test @DisplayName("cancelOrder returns failure when engine fails")
    void cancelOrderFailure() {
        when(engine.cancelOrder(anyLong(), any())).thenReturn(false);
        Result<Void> result = service.cancelOrder(session, 999L);
        assertTrue(result.isFailure());
    }

    @Test @DisplayName("getTradeHistory returns trades from repository")
    void tradeHistory() {
        Trade trade = new Trade(1, 1, "AAPL", TradeType.BUY, 10, new BigDecimal("185.40"));
        when(tradeRepo.findByUserId(1L)).thenReturn(List.of(trade));
        Result<List<Trade>> result = service.getTradeHistory(session);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getValue().size());
    }
}
