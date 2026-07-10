package repository;

import util.DatabaseConnection;

import model.order.BuyOrder;
import model.order.Order;
import model.order.OrderStatus;
import model.order.OrderType;
import model.order.SellOrder;
import model.order.StopLossOrder;
import model.trade.TradeType;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link OrderRepository}.
 * <p>
 * Handles polymorphic order persistence: the {@code execution_type}
 * column (MARKET, LIMIT, STOP_LOSS) discriminates which Order subclass
 * to reconstruct. The {@code trade_type} column captures BUY or SELL.
 * For STOP_LOSS orders, {@code stop_price} stores the trigger threshold.
 */
public class SqliteOrderRepository implements OrderRepository {

    private final DatabaseConnection db;

    private static final String COLUMNS =
        "id, user_id, symbol, trade_type, execution_type, quantity, "
      + "price, stop_price, status, created_at, filled_at";

    private static final String SELECT_BY_ID =
        "SELECT " + COLUMNS + " FROM orders WHERE id = ?";

    private static final String SELECT_BY_USER =
        "SELECT " + COLUMNS + " FROM orders WHERE user_id = ? "
      + "ORDER BY created_at DESC";

    private static final String SELECT_BY_STATUS =
        "SELECT " + COLUMNS + " FROM orders WHERE status = ? "
      + "ORDER BY created_at ASC";

    private static final String UPSERT_SQL =
        "INSERT INTO orders "
      + "(id, user_id, symbol, trade_type, execution_type, quantity, "
      + " price, stop_price, status, created_at, filled_at) "
      + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
      + "ON CONFLICT(id) DO UPDATE SET "
      + "  status    = excluded.status, "
      + "  filled_at = excluded.filled_at";

    public SqliteOrderRepository(DatabaseConnection db) {
        this.db = Objects.requireNonNull(db, "DatabaseConnection must not be null");
    }

    @Override
    public Order save(Order order) {
        String executionType  = order.getOrderType().name();
        String tradeType      = order.getTradeType().name();
        BigDecimal price      = order.getPrice();
        BigDecimal stopPrice  = null;

        if (order instanceof StopLossOrder) {
            stopPrice = ((StopLossOrder) order).getStopPrice();
            price     = BigDecimal.ZERO;
        }

        try (PreparedStatement ps = db.getConnection().prepareStatement(UPSERT_SQL)) {
            ps.setLong(1,    order.getId());
            ps.setLong(2,    order.getUserId());
            ps.setString(3,  order.getSymbol());
            ps.setString(4,  tradeType);
            ps.setString(5,  executionType);
            ps.setInt(6,     order.getQuantity());
            ps.setDouble(7,  price.doubleValue());

            if (stopPrice != null) {
                ps.setDouble(8, stopPrice.doubleValue());
            } else {
                ps.setNull(8, java.sql.Types.REAL);
            }

            ps.setString(9,  order.getStatus().name());
            ps.setString(10, order.getCreatedAt().toString());

            if (order.getFilledAt() != null) {
                ps.setString(11, order.getFilledAt().toString());
            } else {
                ps.setNull(11, java.sql.Types.VARCHAR);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save order: " + order.getId(), e);
        }
        return order;
    }

    @Override
    public Optional<Order> findById(long id) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByUserId(long userId) {
        List<Order> results = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(SELECT_BY_USER)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to find orders for user: " + userId, e);
        }
        return results;
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        List<Order> results = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(SELECT_BY_STATUS)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to find orders with status: " + status, e);
        }
        return results;
    }

    @Override
    public boolean updateStatus(long orderId, OrderStatus newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setLong(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to update order " + orderId + " to " + newStatus, e);
        }
    }

    // ── Row mapping ──────────────────────────────────────────

    private Order mapRow(ResultSet rs) throws SQLException {
        long id             = rs.getLong("id");
        long userId         = rs.getLong("user_id");
        String symbol       = rs.getString("symbol");
        TradeType tradeType = TradeType.valueOf(rs.getString("trade_type"));
        OrderType execType  = OrderType.valueOf(rs.getString("execution_type"));
        int quantity        = rs.getInt("quantity");
        BigDecimal price    = BigDecimal.valueOf(rs.getDouble("price"));
        OrderStatus status  = OrderStatus.valueOf(rs.getString("status"));

        // Reconstruct the correct subclass
        Order order;
        if (execType == OrderType.STOP_LOSS) {
            BigDecimal stopPrice = BigDecimal.valueOf(
                rs.getDouble("stop_price"));
            order = new StopLossOrder(
                id, userId, symbol, quantity, stopPrice, tradeType);
        } else if (tradeType == TradeType.BUY) {
            order = new BuyOrder(
                id, userId, symbol, quantity, price, execType);
        } else {
            order = new SellOrder(
                id, userId, symbol, quantity, price, execType);
        }

        // Restore persisted state
        String filledAtStr = rs.getString("filled_at");
        LocalDateTime filledAt = (filledAtStr != null)
            ? LocalDateTime.parse(filledAtStr) : null;
        order.restoreStatus(status, filledAt);

        return order;
    }
}
