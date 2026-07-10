package model.stock;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a common equity stock in the trading system.
 * <p>
 * An EquityStock is a type of Stock that represents ownership
 * shares in a publicly traded corporation. It extends Stock with
 * market capitalisation and outstanding shares information.
 */
public class EquityStock extends Stock {

    private long marketCap;
    private long sharesOutstanding;

    /**
     * Constructs an EquityStock with the specified attributes.
     *
     * @param symbol            the ticker symbol
     * @param companyName       the full company name
     * @param sector            the industry sector
     * @param currentPrice      the initial market price
     * @param marketCap         the market capitalisation in USD
     * @param sharesOutstanding the number of outstanding shares
     * @throws NullPointerException     if any required argument is null
     * @throws IllegalArgumentException if any numeric value is negative
     */
    public EquityStock(String symbol, String companyName, String sector,
                       BigDecimal currentPrice, long marketCap,
                       long sharesOutstanding) {
        super(symbol, companyName, sector, currentPrice);
        if (marketCap < 0) {
            throw new IllegalArgumentException("Market cap cannot be negative");
        }
        if (sharesOutstanding < 0) {
            throw new IllegalArgumentException(
                "Shares outstanding cannot be negative");
        }
        this.marketCap = marketCap;
        this.sharesOutstanding = sharesOutstanding;
    }

    /**
     * Returns the market capitalisation in USD.
     */
    public long getMarketCap() {
        return marketCap;
    }

    /**
     * Updates the market capitalisation.
     */
    public void setMarketCap(long marketCap) {
        if (marketCap < 0) {
            throw new IllegalArgumentException("Market cap cannot be negative");
        }
        this.marketCap = marketCap;
    }

    /**
     * Returns the number of outstanding shares.
     */
    public long getSharesOutstanding() {
        return sharesOutstanding;
    }

    /**
     * Updates the number of outstanding shares.
     */
    public void setSharesOutstanding(long sharesOutstanding) {
        if (sharesOutstanding < 0) {
            throw new IllegalArgumentException(
                "Shares outstanding cannot be negative");
        }
        this.sharesOutstanding = sharesOutstanding;
    }

    @Override
    public String getAssetType() {
        return "Equity";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EquityStock that = (EquityStock) o;
        return marketCap == that.marketCap
            && sharesOutstanding == that.sharesOutstanding;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), marketCap, sharesOutstanding);
    }
}
