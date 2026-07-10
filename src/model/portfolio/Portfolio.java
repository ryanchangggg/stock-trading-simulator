package model.portfolio;

import model.exception.InvalidOrderException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a user's collection of stock holdings.
 * <p>
 * A Portfolio aggregates multiple Position objects and provides
 * methods to add, remove, and query holdings. It also calculates
 * aggregate portfolio metrics such as total value and unrealised
 * P&L across all positions. The portfolio enforces invariants such
 * as not allowing sales that exceed the current quantity held.
 */
public class Portfolio {

    private final long userId;
    private final List<Position> positions;
    private final LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    /**
     * Constructs an empty Portfolio for the specified user.
     *
     * @param userId the identifier of the user who owns this portfolio
     */
    public Portfolio(long userId) {
        this.userId = userId;
        this.positions = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Returns the user identifier who owns this portfolio.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Returns an unmodifiable view of all positions in this portfolio.
     */
    public List<Position> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    /**
     * Returns the creation timestamp.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the timestamp of the most recent portfolio change.
     */
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    /**
     * Finds a position by stock symbol.
     *
     * @param symbol the stock ticker symbol
     * @return an Optional containing the Position if found, or empty
     */
    public Optional<Position> getPosition(String symbol) {
        Objects.requireNonNull(symbol, "Symbol must not be null");
        return positions.stream()
            .filter(p -> p.getSymbol().equalsIgnoreCase(symbol))
            .findFirst();
    }

    /**
     * Returns true if the portfolio holds any shares of the given symbol.
     *
     * @param symbol the stock ticker symbol
     */
    public boolean holdsSymbol(String symbol) {
        return getPosition(symbol)
            .map(p -> p.getQuantity() > 0)
            .orElse(false);
    }

    /**
     * Adds shares of a stock to the portfolio.
     * <p>
     * If a position for the symbol already exists, the shares are added
     * to it and the average cost is recalculated. Otherwise, a new
     * position is created.
     *
     * @param symbol        the stock ticker symbol
     * @param quantity      the number of shares to add
     * @param purchasePrice the price per share paid
     */
    public void addShares(String symbol, int quantity, BigDecimal purchasePrice) {
        Objects.requireNonNull(symbol, "Symbol must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Added quantity must be positive");
        }
        Objects.requireNonNull(purchasePrice,
            "Purchase price must not be null");
        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Purchase price cannot be negative");
        }
        Optional<Position> existing = getPosition(symbol);
        if (existing.isPresent()) {
            existing.get().addShares(quantity, purchasePrice);
        } else {
            positions.add(new Position(symbol, quantity, purchasePrice));
        }
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Removes shares of a stock from the portfolio.
     * <p>
     * Validates that the user holds enough shares before removing.
     * If all shares are removed, the position is removed from the list.
     *
     * @param symbol   the stock ticker symbol
     * @param quantity the number of shares to remove
     * @throws InvalidOrderException if the user does not hold enough shares
     */
    public void removeShares(String symbol, int quantity) {
        Objects.requireNonNull(symbol, "Symbol must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Removed quantity must be positive");
        }
        Position pos = getPosition(symbol)
            .orElseThrow(() -> new InvalidOrderException(
                "No position found for symbol: " + symbol));
        if (pos.getQuantity() < quantity) {
            throw new InvalidOrderException(
                "Insufficient shares: held=" + pos.getQuantity()
                    + ", requested=" + quantity);
        }
        pos.removeShares(quantity);
        if (pos.isEmpty()) {
            positions.remove(pos);
        }
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Returns the total number of distinct stock symbols held.
     */
    public int getPositionCount() {
        return positions.size();
    }

    /**
     * Returns the total number of shares across all positions.
     */
    public int getTotalShares() {
        return positions.stream()
            .mapToInt(Position::getQuantity)
            .sum();
    }

    /**
     * Calculates and returns a summary snapshot of the portfolio.
     *
     * @param priceProvider a function that returns the current price
     *                      for a given stock symbol
     * @return a PortfolioSummary with calculated metrics
     */
    public PortfolioSummary getSummary(PriceProvider priceProvider) {
        Objects.requireNonNull(priceProvider,
            "Price provider must not be null");
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Position pos : positions) {
            BigDecimal currentPrice = priceProvider.getPrice(pos.getSymbol());
            totalValue = totalValue.add(pos.getMarketValue(currentPrice));
            totalCost = totalCost.add(pos.getTotalCost());
        }

        return new PortfolioSummary(
            totalValue,
            totalCost,
            totalValue.subtract(totalCost),
            positions.size(),
            getTotalShares()
        );
    }

    @Override
    public String toString() {
        return "Portfolio{userId=" + userId
            + ", positions=" + positions.size()
            + ", totalShares=" + getTotalShares() + '}';
    }

    /**
     * Functional interface for providing current stock prices
     * when computing portfolio summaries.
     */
    @FunctionalInterface
    public interface PriceProvider {
        /**
         * Returns the current price for the given stock symbol.
         */
        BigDecimal getPrice(String symbol);
    }

    /**
     * An immutable snapshot of portfolio-level calculated metrics.
     */
    public static class PortfolioSummary {
        private final BigDecimal totalValue;
        private final BigDecimal totalCost;
        private final BigDecimal unrealisedPnl;
        private final BigDecimal unrealisedPnlPercent;
        private final int positionCount;
        private final int totalShares;

        PortfolioSummary(BigDecimal totalValue, BigDecimal totalCost,
                         BigDecimal unrealisedPnl, int positionCount,
                         int totalShares) {
            this.totalValue = totalValue;
            this.totalCost = totalCost;
            this.unrealisedPnl = unrealisedPnl;
            this.positionCount = positionCount;
            this.totalShares = totalShares;
            this.unrealisedPnlPercent = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? unrealisedPnl.divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        }

        public BigDecimal getTotalValue() { return totalValue; }
        public BigDecimal getTotalCost() { return totalCost; }
        public BigDecimal getUnrealisedPnl() { return unrealisedPnl; }
        public BigDecimal getUnrealisedPnlPercent() { return unrealisedPnlPercent; }
        public int getPositionCount() { return positionCount; }
        public int getTotalShares() { return totalShares; }
    }
}
