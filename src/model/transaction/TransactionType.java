package model.transaction;

/**
 * Enumerates the types of financial transactions in the trading system.
 * <p>
 * DEPOSIT adds funds to an account. WITHDRAWAL removes funds.
 * TRADE_SETTLEMENT represents the cash impact of a completed trade.
 * FEE represents a service charge.
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRADE_SETTLEMENT,
    FEE
}
