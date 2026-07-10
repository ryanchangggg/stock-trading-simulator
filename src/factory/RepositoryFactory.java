package factory;

import repository.OrderRepository;
import repository.PortfolioRepository;
import repository.SqliteOrderRepository;
import repository.SqlitePortfolioRepository;
import repository.SqliteTradeHistoryRepository;
import repository.SqliteUserRepository;
import repository.TradeRepository;
import repository.UserRepository;
import util.DatabaseConnection;
import java.util.Objects;

/**
 * Convenience factory for creating all SQLite-backed repository
 * implementations with a single shared database connection.
 * <p>
 * Initialises the database schema on construction, then provides
 * ready-to-use repository instances.
 */
public class RepositoryFactory {

    private final DatabaseConnection db;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    public RepositoryFactory(String dbPath) {
        Objects.requireNonNull(dbPath, "Database path must not be null");
        this.db = DatabaseConnection.getInstance(dbPath);
        this.db.init();
        this.userRepository      = new SqliteUserRepository(db);
        this.portfolioRepository = new SqlitePortfolioRepository(db);
        this.orderRepository     = new SqliteOrderRepository(db);
        this.tradeRepository     = new SqliteTradeHistoryRepository(db);
    }

    public RepositoryFactory() {
        this("stock_trading.db");
    }

    public UserRepository getUserRepository() { return userRepository; }
    public PortfolioRepository getPortfolioRepository() { return portfolioRepository; }
    public OrderRepository getOrderRepository() { return orderRepository; }
    public TradeRepository getTradeRepository() { return tradeRepository; }
    public DatabaseConnection getDatabaseConnection() { return db; }
}
