package model.stock;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a financial security available for trading.
 * <p>
 * Serves as the abstract base class for all financial instruments
 * in the system. Each stock has a ticker symbol, company name,
 * sector classification, and a current market price.
 * Subclasses define the specific asset type (equity, ETF, index, etc.).
 */
public abstract class Stock {

    private final String symbol;
    private final String companyName;
    private final String sector;
    private BigDecimal currentPrice;

    /**
     * Constructs a Stock with the specified attributes.
     *
     * @param symbol       the ticker symbol (e.g., "AAPL")
     * @param companyName  the full company name
     * @param sector       the industry sector
     * @param currentPrice the initial market price; must be non-negative
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if currentPrice is negative
     */
    protected Stock(String symbol, String companyName, String sector,
                    BigDecimal currentPrice) {
        this.symbol = Objects.requireNonNull(symbol, "Symbol must not be null");
        this.companyName = Objects.requireNonNull(companyName,
            "Company name must not be null");
        this.sector = Objects.requireNonNull(sector, "Sector must not be null");
        setCurrentPrice(currentPrice);
    }

    /**
     * Returns the ticker symbol for this stock.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the full company name.
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Returns the industry sector.
     */
    public String getSector() {
        return sector;
    }

    /**
     * Returns the current market price.
     */
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Updates the current market price.
     *
     * @param currentPrice the new price; must be non-negative
     * @throws NullPointerException     if currentPrice is null
     * @throws IllegalArgumentException if currentPrice is negative
     */
    public void setCurrentPrice(BigDecimal currentPrice) {
        Objects.requireNonNull(currentPrice, "Current price must not be null");
        if (currentPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Current price cannot be negative");
        }
        this.currentPrice = currentPrice;
    }

    /**
     * Returns a human-readable label for the asset type.
     * Subclasses must override this to identify their specific type.
     *
     * @return the asset type name (e.g., "Equity")
     */
    public abstract String getAssetType();

    @Override
    public String toString() {
        return getAssetType() + "{symbol='" + symbol + '\''
            + ", company='" + companyName + '\''
            + ", price=" + currentPrice + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return symbol.equals(stock.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
}
