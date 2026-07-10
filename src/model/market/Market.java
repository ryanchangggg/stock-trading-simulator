package model.market;

import model.stock.Stock;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the entire stock market in the simulation.
 * <p>
 * The Market aggregates all available stocks and maintains their
 * current prices. It serves as the central registry for securities
 * in the system and provides lookup, price query, and bulk update
 * operations. The market operates in a simulation context, meaning
 * prices are driven by loaded historical data rather than live feeds.
 * <p>
 * This class follows the Singleton pattern conceptually as there
 * is exactly one market in the simulation.
 */
public class Market {

    private final Map<String, Stock> stocks;
    private final List<String> symbols;
    private LocalDate currentDate;

    /**
     * Constructs an empty Market with no stocks.
     * Stocks are loaded into the market during initialisation.
     */
    public Market() {
        this.stocks = new HashMap<>();
        this.symbols = new ArrayList<>();
        this.currentDate = null;
    }

    /**
     * Adds a stock to the market.
     * <p>
     * If a stock with the same symbol already exists, it is replaced.
     *
     * @param stock the stock to add
     * @throws NullPointerException if stock is null
     */
    public void addStock(Stock stock) {
        Objects.requireNonNull(stock, "Stock must not be null");
        String symbol = stock.getSymbol();
        if (!stocks.containsKey(symbol)) {
            symbols.add(symbol);
        }
        stocks.put(symbol, stock);
    }

    /**
     * Removes a stock from the market by symbol.
     *
     * @param symbol the stock ticker symbol to remove
     */
    public void removeStock(String symbol) {
        Objects.requireNonNull(symbol, "Symbol must not be null");
        if (stocks.remove(symbol) != null) {
            symbols.remove(symbol);
        }
    }

    /**
     * Looks up a stock by its ticker symbol.
     *
     * @param symbol the stock ticker symbol
     * @return an Optional containing the Stock if found, or empty
     */
    public Optional<Stock> getStock(String symbol) {
        Objects.requireNonNull(symbol, "Symbol must not be null");
        return Optional.ofNullable(stocks.get(symbol.toUpperCase()));
    }

    /**
     * Returns the current price of the specified stock.
     *
     * @param symbol the stock ticker symbol
     * @return the current market price
     * @throws IllegalArgumentException if the stock is not found
     */
    public BigDecimal getCurrentPrice(String symbol) {
        return getStock(symbol)
            .map(Stock::getCurrentPrice)
            .orElseThrow(() -> new IllegalArgumentException(
                "Stock not found: " + symbol));
    }

    /**
     * Updates the current price of a stock.
     *
     * @param symbol the stock ticker symbol
     * @param price  the new market price
     * @throws IllegalArgumentException if the stock is not found
     */
    public void updatePrice(String symbol, BigDecimal price) {
        Objects.requireNonNull(price, "Price must not be null");
        Stock stock = getStock(symbol)
            .orElseThrow(() -> new IllegalArgumentException(
                "Stock not found: " + symbol));
        stock.setCurrentPrice(price);
    }

    /**
     * Returns an unmodifiable list of all stock symbols in the market.
     */
    public List<String> getAllSymbols() {
        return Collections.unmodifiableList(symbols);
    }

    /**
     * Returns an unmodifiable list of all stocks in the market.
     */
    public List<Stock> getAllStocks() {
        return Collections.unmodifiableList(new ArrayList<>(stocks.values()));
    }

    /**
     * Returns the number of stocks listed in the market.
     */
    public int getStockCount() {
        return stocks.size();
    }

    /**
     * Returns the current simulation date.
     */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    /**
     * Sets the current simulation date.
     *
     * @param currentDate the date to set
     */
    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    /**
     * Returns true if the market has any stocks loaded.
     */
    public boolean isEmpty() {
        return stocks.isEmpty();
    }

    /**
     * Clears all stocks from the market.
     */
    public void clear() {
        stocks.clear();
        symbols.clear();
        currentDate = null;
    }

    @Override
    public String toString() {
        return "Market{stocks=" + stocks.size()
            + ", date=" + currentDate + '}';
    }
}
