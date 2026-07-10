package repository;

import util.DatabaseConnection;

import model.trade.Trade;
import model.trade.TradeType;
import repository.TradeRepository;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SQLite-backed implementation of {@link TradeRepository}.
 * <p>
 * Persists completed trades to the {@code trade_history} table,
 * providing a durable audit trail for all market activity.
 */
public class SqliteTradeHistoryRepository implements TradeRepository {

    private final DatabaseConnection db;

    public SqliteTradeHistoryRepository(DatabaseConnection db) {
        this.db = Objects.requireNonNull(db, "DatabaseConnection must not be null");
    }

    @Override
    public Trade save(Trade trade) {
        String sql = "INSERT INTO trade_history "
            + "(id, user_id, symbol, trade_type, quantity, price, "
            + " total_value, trade_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, trade.getId());
            ps.setLong(2, trade.getUserId());
            ps.setString(3, trade.getSymbol());
            ps.setString(4, trade.getTradeType().name());
            ps.setInt(5, trade.getQuantity());
            ps.setDouble(6, trade.getPrice().doubleValue());
            ps.setDouble(7, trade.getTotalValue().doubleValue());
            ps.setString(8, trade.getTradeDate().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to save trade: " + trade.getId(), e);
        }
        return trade;
    }

    @Override
    public List<Trade> findByUserId(long userId) {
        String sql = "SELECT id, user_id, symbol, trade_type, quantity, price "
            + "FROM trade_history WHERE user_id = ? ORDER BY timestamp DESC";
        List<Trade> results = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to find trades for user: " + userId, e);
        }
        return results;
    }

    @Override
    public List<Trade> findBySymbol(String symbol) {
        String sql = "SELECT id, user_id, symbol, trade_type, quantity, price "
            + "FROM trade_history WHERE symbol = ? ORDER BY timestamp DESC";
        List<Trade> results = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, symbol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to find trades for symbol: " + symbol, e);
        }
        return results;
    }

    private Trade mapRow(ResultSet rs) throws SQLException {
        long id             = rs.getLong("id");
        long userId         = rs.getLong("user_id");
        String symbol       = rs.getString("symbol");
        TradeType tradeType = TradeType.valueOf(rs.getString("trade_type"));
        int quantity        = rs.getInt("quantity");
        BigDecimal price    = BigDecimal.valueOf(rs.getDouble("price"));
        return new Trade(id, userId, symbol, tradeType, quantity, price);
    }
}
