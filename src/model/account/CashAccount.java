package model.account;

import java.math.BigDecimal;

/**
 * A standard cash-based trading account.
 * <p>
 * CashAccount is the default account type in the system. It supports
 * deposits and withdrawals within the available balance. This is the
 * only account type in v1.0; future versions may introduce MarginAccount
 * and other subtypes.
 * <p>
 * Each new user is assigned a CashAccount with a configurable starting
 * balance (default $100,000.00).
 */
public class CashAccount extends Account {

    /** Default starting balance for new cash accounts. */
    public static final BigDecimal DEFAULT_STARTING_BALANCE =
        new BigDecimal("100000.00");

    /**
     * Constructs a CashAccount with the default starting balance in USD.
     */
    public CashAccount() {
        super("USD", DEFAULT_STARTING_BALANCE);
    }

    /**
     * Constructs a CashAccount with a specified initial balance in USD.
     *
     * @param initialBalance the starting balance; must be non-negative
     */
    public CashAccount(BigDecimal initialBalance) {
        super("USD", initialBalance);
    }

    /**
     * Constructs a CashAccount with a specified currency and initial balance.
     *
     * @param currency       the ISO 4217 currency code
     * @param initialBalance the starting balance; must be non-negative
     */
    public CashAccount(String currency, BigDecimal initialBalance) {
        super(currency, initialBalance);
    }

    @Override
    public String getAccountType() {
        return "CashAccount";
    }
}
