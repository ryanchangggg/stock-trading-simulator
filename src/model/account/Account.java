package model.account;

import model.exception.InsufficientFundsException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a financial account in the trading system.
 * <p>
 * Serves as the abstract base class for all account types (cash, margin, etc.).
 * Encapsulates balance management with deposit and withdrawal operations
 * that enforce domain invariants such as non-negative balances.
 * Each account has a unique identifier, a currency, and tracks creation
 * and last-updated timestamps.
 */
public abstract class Account {

    private final String accountId;
    private BigDecimal balance;
    private final String currency;
    private final LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    /**
     * Constructs an Account with the given currency and initial balance.
     *
     * @param currency       the ISO 4217 currency code (e.g., "USD")
     * @param initialBalance the starting balance; must be non-negative
     * @throws NullPointerException     if currency or initialBalance is null
     * @throws IllegalArgumentException if initialBalance is negative
     */
    protected Account(String currency, BigDecimal initialBalance) {
        this.accountId = UUID.randomUUID().toString();
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
        this.balance = Objects.requireNonNull(initialBalance, "Initial balance must not be null");
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Returns the unique identifier for this account.
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Returns the current balance of this account.
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Returns the ISO 4217 currency code for this account.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Returns the timestamp when this account was created.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the timestamp of the most recent balance change.
     */
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    /**
     * Deposits funds into this account.
     * <p>
     * Increases the balance by the specified amount.
     *
     * @param amount the amount to deposit; must be positive
     * @throws NullPointerException     if amount is null
     * @throws IllegalArgumentException if amount is not positive
     */
    public void deposit(BigDecimal amount) {
        Objects.requireNonNull(amount, "Deposit amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Withdraws funds from this account.
     * <p>
     * Decreases the balance by the specified amount. Throws
     * InsufficientFundsException if the balance is too low.
     *
     * @param amount the amount to withdraw; must be positive
     * @return true if the withdrawal was successful
     * @throws NullPointerException        if amount is null
     * @throws IllegalArgumentException    if amount is not positive
     * @throws InsufficientFundsException  if the balance is insufficient
     */
    public boolean withdraw(BigDecimal amount) {
        Objects.requireNonNull(amount, "Withdrawal amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                "Insufficient funds: balance=" + this.balance + ", required=" + amount
            );
        }
        this.balance = this.balance.subtract(amount);
        this.lastUpdatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * Returns a human-readable label for this account type.
     * Subclasses must override this to identify their specific type.
     *
     * @return the account type name (e.g., "CashAccount")
     */
    public abstract String getAccountType();

    @Override
    public String toString() {
        return getAccountType() + "{"
            + "accountId='" + accountId + '\''
            + ", balance=" + balance
            + ", currency='" + currency + '\''
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountId.equals(account.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}
