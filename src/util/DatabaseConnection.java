package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Manages the SQLite database connection and schema initialisation.
 * <p>
 * Implements the Singleton pattern so the entire application shares
 * one database connection. On first access, it connects to the SQLite
 * file and runs schema migration: drops legacy capitalized tables,
 * creates the current lowercase tables with updated columns
 * (including {@code execution_type} and {@code stop_price} for orders),
 * and creates performance indexes.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private final String dbPath;
    private Connection connection;

    private static final String SCHEMA_USERS =
        "CREATE TABLE IF NOT EXISTS users ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "username TEXT NOT NULL UNIQUE,"
        + "password_hash TEXT NOT NULL,"
        + "email TEXT,"
        + "cash_balance REAL NOT NULL DEFAULT 100000.00,"
        + "created_at TEXT NOT NULL DEFAULT (datetime('now'))"
        + ")";

    private static final String SCHEMA_PORTFOLIOS =
        "CREATE TABLE IF NOT EXISTS portfolios ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "user_id INTEGER NOT NULL,"
        + "symbol TEXT NOT NULL COLLATE NOCASE,"
        + "quantity INTEGER NOT NULL DEFAULT 0,"
        + "average_cost REAL NOT NULL DEFAULT 0.0,"
        + "last_updated TEXT NOT NULL DEFAULT (datetime('now')),"
        + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
        + "UNIQUE(user_id, symbol)"
        + ")";

    private static final String SCHEMA_ORDERS =
        "CREATE TABLE IF NOT EXISTS orders ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "user_id INTEGER NOT NULL,"
        + "symbol TEXT NOT NULL COLLATE NOCASE,"
        + "trade_type TEXT NOT NULL CHECK(trade_type IN ('BUY','SELL')),"
        + "execution_type TEXT NOT NULL"
        + "  CHECK(execution_type IN ('MARKET','LIMIT','STOP_LOSS')),"
        + "quantity INTEGER NOT NULL,"
        + "price REAL NOT NULL,"
        + "stop_price REAL,"
        + "status TEXT NOT NULL DEFAULT 'PENDING'"
        + "  CHECK(status IN ('PENDING','FILLED','CANCELLED')),"
        + "created_at TEXT NOT NULL DEFAULT (datetime('now')),"
        + "filled_at TEXT,"
        + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        + ")";

    private static final String SCHEMA_TRADE_HISTORY =
        "CREATE TABLE IF NOT EXISTS trade_history ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "user_id INTEGER NOT NULL,"
        + "symbol TEXT NOT NULL COLLATE NOCASE,"
        + "trade_type TEXT NOT NULL CHECK(trade_type IN ('BUY','SELL')),"
        + "quantity INTEGER NOT NULL,"
        + "price REAL NOT NULL,"
        + "total_value REAL NOT NULL,"
        + "trade_date TEXT NOT NULL,"
        + "timestamp TEXT NOT NULL DEFAULT (datetime('now')),"
        + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        + ")";

    private static final String[] DROP_LEGACY = {
        "DROP TABLE IF EXISTS Orders",
        "DROP TABLE IF EXISTS Portfolio",
        "DROP TABLE IF EXISTS TradeHistory",
        "DROP TABLE IF EXISTS User"
    };

    private static final String[] CREATE_INDEXES = {
        "CREATE INDEX IF NOT EXISTS idx_portfolios_user ON portfolios(user_id)",
        "CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id)",
        "CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)",
        "CREATE INDEX IF NOT EXISTS idx_trade_history_user"
            + " ON trade_history(user_id)",
        "CREATE INDEX IF NOT EXISTS idx_trade_history_symbol"
            + " ON trade_history(symbol)"
    };

    /**
     * Constructs or connects to the specified SQLite database.
     *
     * @param dbPath filesystem path to the .db file
     */
    private DatabaseConnection(String dbPath) {
        this.dbPath = Objects.requireNonNull(dbPath, "DB path must not be null");
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection("stock_trading.db");
        }
        return instance;
    }

    public static synchronized DatabaseConnection getInstance(String dbPath) {
        if (instance == null || !instance.dbPath.equals(dbPath)) {
            instance = new DatabaseConnection(dbPath);
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + dbPath);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            } catch (SQLException e) {
                throw new RuntimeException(
                    "Failed to connect to database: " + dbPath, e);
            }
        }
        return connection;
    }

    /**
     * Initialises the database schema. Safe to call multiple times.
     * Drops legacy capitalized tables and creates the current schema.
     */
    public synchronized void init() {
        try (Statement stmt = getConnection().createStatement()) {
            // Drop legacy-capitalised tables
            for (String drop : DROP_LEGACY) {
                stmt.execute(drop);
            }

            // Create current tables
            stmt.execute(SCHEMA_USERS);
            stmt.execute(SCHEMA_PORTFOLIOS);
            stmt.execute(SCHEMA_ORDERS);
            stmt.execute(SCHEMA_TRADE_HISTORY);

            // Create indexes
            for (String idx : CREATE_INDEXES) {
                stmt.execute(idx);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to initialise database schema", e);
        }
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }
}
