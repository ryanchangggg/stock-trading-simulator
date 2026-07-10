package model.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents a holding position in a single stock within a portfolio.
 * <p>
 * A Position tracks the number of shares held, the average cost basis
 * per share, and is able to calculate the current market value and
 * unrealised profit or loss when given the current market price.
 * Positions are value objects within a Portfolio.
 */
public class Position {

    private final String symbol;
    private int quantity;
    private BigDecimal averageCost;

    /**
     * Constructs a Position with the specified attributes.
     *
     * @param symbol      the stock ticker symbol
     * @param quantity    the number of shares held; must be non-negative
     * @param averageCost the average cost per share; must be non-negative
     * @throws NullPointerException     if symbol or averageCost is null
     * @throws IllegalArgumentException if quantity or averageCost is negative
     */
    public Position(String symbol, int quantity, BigDecimal averageCost) {
        this.symbol = Objects.requireNonNull(symbol, "Symbol must not be null");
        if (quantity < 0) {
            throw new IllegalArgumentException(
                "Position quantity cannot be negative");
        }
        this.quantity = quantity;
        this.averageCost = Objects.requireNonNull(averageCost,
            "Average cost must not be null");
        if (averageCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Average cost cannot be negative");
        }
    }

    /**
     * Returns the stock ticker symbol for this position.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the number of shares currently held.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the average cost basis per share.
     */
    public BigDecimal getAverageCost() {
        return averageCost;
    }

    /**
     * Returns the total cost basis (quantity * average cost).
     */
    public BigDecimal getTotalCost() {
        return averageCost.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Returns the current market value (quantity * current price).
     *
     * @param currentPrice the current market price per share
     * @return the total market value
     */
    public BigDecimal getMarketValue(BigDecimal currentPrice) {
        Objects.requireNonNull(currentPrice, "Current price must not be null");
        return currentPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Returns the unrealised profit or loss (market value - total cost).
     * A positive value indicates an unrealised gain; negative indicates a loss.
     *
     * @param currentPrice the current market price per share
     * @return the unrealised P&L
     */
    public BigDecimal getUnrealisedPnl(BigDecimal currentPrice) {
        Objects.requireNonNull(currentPrice, "Current price must not be null");
        return getMarketValue(currentPrice).subtract(getTotalCost());
    }

    /**
     * Returns the unrealised profit or loss as a percentage of cost basis.
     *
     * @param currentPrice the current market price per share
     * @return the percentage return (e.g., 15.25 for a 15.25% gain)
     */
    public BigDecimal getUnrealisedPnlPercent(BigDecimal currentPrice) {
        Objects.requireNonNull(currentPrice, "Current price must not be null");
        if (getTotalCost().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getUnrealisedPnl(currentPrice)
            .divide(getTotalCost(), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }

    /**
     * Adds shares to this position, updating the average cost basis.
     * <p>
     * The new average cost is calculated as a weighted average
     * of the existing position and the new purchase.
     *
     * @param addedQuantity the number of additional shares
     * @param purchasePrice the price per share of the new purchase
     * @throws IllegalArgumentException if addedQuantity is not positive,
     *                                  or purchasePrice is negative
     */
    public void addShares(int addedQuantity, BigDecimal purchasePrice) {
        if (addedQuantity <= 0) {
            throw new IllegalArgumentException(
                "Added quantity must be positive");
        }
        Objects.requireNonNull(purchasePrice, "Purchase price must not be null");
        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Purchase price cannot be negative");
        }
        BigDecimal totalCost = getTotalCost()
            .add(purchasePrice.multiply(BigDecimal.valueOf(addedQuantity)));
        this.quantity += addedQuantity;
        this.averageCost = totalCost.divide(
            BigDecimal.valueOf(this.quantity), 2, RoundingMode.HALF_UP);
    }

    /**
     * Removes shares from this position.
     * <p>
     * The average cost basis remains unchanged; only the quantity is reduced.
     *
     * @param removedQuantity the number of shares to remove
     * @throws IllegalArgumentException if removedQuantity is not positive
     *                                  or exceeds the current quantity
     */
    public void removeShares(int removedQuantity) {
        if (removedQuantity <= 0) {
            throw new IllegalArgumentException(
                "Removed quantity must be positive");
        }
        if (removedQuantity > this.quantity) {
            throw new IllegalArgumentException(
                "Cannot remove " + removedQuantity + " shares; only "
                    + this.quantity + " held");
        }
        this.quantity -= removedQuantity;
    }

    /**
     * Returns true if this position holds zero shares.
     */
    public boolean isEmpty() {
        return quantity == 0;
    }

    @Override
    public String toString() {
        return "Position{symbol='" + symbol + '\''
            + ", quantity=" + quantity
            + ", avgCost=" + averageCost + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return symbol.equals(position.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
}
