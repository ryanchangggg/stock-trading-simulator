package service;

import service.TradingEngine;
import model.market.Market;
import util.CsvDataLoader;
import factory.RepositoryFactory;
import java.io.IOException;

/**
 * Application-wide dependency container.
 * <p>
 * Wires together database, engine, market, and services at startup.
 * The UI layer accesses all dependencies through this single context,
 * ensuring consistent injection across the application.
 * Market data is loaded automatically from the data/ directory.
 */
public class AppContext {

    private final RepositoryFactory repositoryFactory;
    private final Market market;
    private final TradingEngine tradingEngine;
    private final AuthenticationService authService;
    private final TradingService tradingService;
    private final PortfolioService portfolioService;
    private final MarketDataService marketDataService;

    public AppContext() {
        this.repositoryFactory = new RepositoryFactory("stock_trading.db");
        this.market = new Market();
        this.tradingEngine = new TradingEngine(
            repositoryFactory.getOrderRepository(),
            repositoryFactory.getTradeRepository(),
            repositoryFactory.getUserRepository());

        this.authService = new AuthenticationService(
            repositoryFactory.getUserRepository());
        this.tradingService = new TradingService(
            tradingEngine,
            repositoryFactory.getOrderRepository(),
            repositoryFactory.getTradeRepository(),
            repositoryFactory.getUserRepository(),
            market);
        this.portfolioService = new PortfolioService(
            repositoryFactory.getPortfolioRepository(),
            repositoryFactory.getUserRepository(),
            market);
        this.marketDataService = new MarketDataService(market);
    }

    /**
     * Loads market data from CSV files in the specified directory.
     * Safe to call multiple times; re-loads all stocks.
     *
     * @param dataDir path to directory containing CSV files
     */
    public void loadMarketData(String dataDir) {
        try {
            new CsvDataLoader(market).loadAll(dataDir);
        } catch (IOException e) {
            System.err.println("Warning: could not load market data from "
                + dataDir + ": " + e.getMessage());
        }
    }

    public AuthenticationService getAuthService() { return authService; }
    public TradingService getTradingService() { return tradingService; }
    public PortfolioService getPortfolioService() { return portfolioService; }
    public MarketDataService getMarketDataService() { return marketDataService; }
    public Market getMarket() { return market; }
    public TradingEngine getEngine() { return tradingEngine; }
}
