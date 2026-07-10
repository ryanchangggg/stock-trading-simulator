package repository;

import util.DatabaseConnection;

import model.portfolio.Portfolio;
import model.portfolio.Position;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * SQLite-backed implementation of {@link PortfolioRepository}.
 * <p>
 * Maps each stock holding (Position) to a row in the {@code portfolios}
 * table. Loading a portfolio queries all rows for a user and
 * reconstructs Position objects. Saving uses a transaction to
 * atomically delete all existing rows and re-insert the current
 * position set.
 */
public class SqlitePortfolioRepository implements PortfolioRepository {

    private final DatabaseConnection db;

    public SqlitePortfolioRepository(DatabaseConnection db) {
        this.db = Objects.requireNonNull(db, "DatabaseConnection must not be null");
    }

    @Override
    public Portfolio findByUserId(long userId) {
        Portfolio portfolio = new Portfolio(userId);
        String sql = "SELECT symbol, quantity, average_cost "
                   + "FROM portfolios WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String symbol = rs.getString("symbol");
                    int quantity = rs.getInt("quantity");
                    BigDecimal avgCost = BigDecimal.valueOf(
                        rs.getDouble("average_cost"));
                    if (quantity > 0) {
                        portfolio.addShares(symbol, quantity, avgCost);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to load portfolio for user: " + userId, e);
        }
        return portfolio;
    }

    @Override
    public void save(Portfolio portfolio) {
        long userId = portfolio.getUserId();
        Connection conn = db.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Wipe existing positions for this user
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM portfolios WHERE user_id = ?")) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }

            // 2. Insert current positions
            String insertSql = "INSERT INTO portfolios "
                + "(user_id, symbol, quantity, average_cost, last_updated) "
                + "VALUES (?, ?, ?, ?, datetime('now'))";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Position pos : portfolio.getPositions()) {
                    ps.setLong(1, userId);
                    ps.setString(2, pos.getSymbol());
                    ps.setInt(3, pos.getQuantity());
                    ps.setDouble(4, pos.getAverageCost().doubleValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) { }
            throw new RuntimeException(
                "Failed to save portfolio for user: " + userId, e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) { }
        }
    }

    @Override
    public void deleteByUserId(long userId) {
        String sql = "DELETE FROM portfolios WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to delete portfolio for user: " + userId, e);
        }
    }
}
