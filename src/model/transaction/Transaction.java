package model.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a financial transaction on a user account.
 * <p>
 * Every change to an account balance is recorded as a Transaction,
 * providing a complete audit trail. Transactions capture the type
 * (deposit, withdrawal, trade settlement, fee), the amount, the
 * resulting balance after the transaction, and a descriptive note.
 * Transactions are immutable once created.
 */
public class Transaction {

    private final long id;
    private final long userId;
    private final String accountId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String description;
    private final LocalDateTime timestamp;

    /**
     * Constructs a Transaction with the specified attributes.
     *
     * @param id           the unique transaction identifier
     * @param userId       the identifier of the user
     * @param accountId    the identifier of the account
     * @param type         the type of transaction
     * @param amount       the amount involved; must be positive
     * @param balanceAfter the account balance after this transaction
     * @param description  a human-readable description
     * @throws NullPointerException     if type, amount, balanceAfter,
     *                                  or description is null
     * @throws IllegalArgumentException if amount is not positive
     */
    public Transaction(long id, long userId, String accountId,
                       TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Transaction amount must be positive");
        }
        this.id = id;
        this.userId = userId;
        this.accountId = Objects.requireNonNull(accountId,
            "Account ID must not be null");
        this.type = Objects.requireNonNull(type, "Type must not be null");
        this.amount = amount;
        this.balanceAfter = Objects.requireNonNull(balanceAfter,
            "Balance after must not be null");
        this.description = Objects.requireNonNull(description,
            "Description must not be null");
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Returns the unique transaction identifier.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the user identifier associated with this transaction.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Returns the account identifier involved in this transaction.
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Returns the type of this transaction.
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Returns the transaction amount.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Returns the account balance after this transaction was applied.
     */
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    /**
     * Returns a human-readable description of this transaction.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the timestamp when this transaction was recorded.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{id=" + id
            + ", type=" + type
            + ", amount=" + amount
            + ", balanceAfter=" + balanceAfter
            + ", desc='" + description + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
