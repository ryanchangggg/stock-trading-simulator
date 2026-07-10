package service;

import model.market.Market;
import model.stock.Stock;
import java.math.BigDecimal;
import java.util.List;

/**
 * Provides market data to the UI layer.
 * <p>
 * Wraps the Market domain object and exposes only read
 * operations needed for display.
 */
public class MarketDataService {

    private final Market market;

    public MarketDataService(Market market) {
        this.market = market;
    }

    public Result<List<Stock>> getAvailableStocks() {
        return Result.success(market.getAllStocks());
    }

    public Result<List<String>> getSymbols() {
        return Result.success(market.getAllSymbols());
    }

    public Result<BigDecimal> getPrice(String symbol) {
        try {
            return Result.success(market.getCurrentPrice(symbol));
        } catch (IllegalArgumentException e) {
            return Result.failure("Stock not found: " + symbol);
        }
    }

    public Result<Stock> getStock(String symbol) {
        return market.getStock(symbol)
            .map(Result::success)
            .orElse(Result.failure("Stock not found: " + symbol));
    }
}
