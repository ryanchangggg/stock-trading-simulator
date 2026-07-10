package repository;

import util.DatabaseConnection;

import model.user.User;
import repository.UserRepository;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link UserRepository}.
 * <p>
 * Maps the User domain model to the {@code users} table. The user's
 * cash balance is persisted from the embedded CashAccount. Password
 * hashes are stored as-is (hashing is a service-layer concern).
 * Portfolio state is not managed here — use {@link SqlitePortfolioRepository}.
 */
public class SqliteUserRepository implements UserRepository {

    private final DatabaseConnection db;

    public SqliteUserRepository(DatabaseConnection db) {
        this.db = Objects.requireNonNull(db, "DatabaseConnection must not be null");
    }

    @Override
    public Optional<User> findById(long id) {
        String sql = "SELECT id, username, password_hash, cash_balance "
                   + "FROM users WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, cash_balance "
                   + "FROM users WHERE username = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to find user by username: " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (id, username, password_hash, cash_balance) "
                   + "VALUES (?, ?, ?, ?) "
                   + "ON CONFLICT(id) DO UPDATE SET "
                   + "  password_hash = excluded.password_hash, "
                   + "  cash_balance  = excluded.cash_balance";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setDouble(4, user.getCashBalance().doubleValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to save user: " + user.getUsername(), e);
        }
        return user;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        BigDecimal cashBalance = BigDecimal.valueOf(rs.getDouble("cash_balance"));
        return new User(id, username, passwordHash, cashBalance);
    }
}
