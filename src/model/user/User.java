package model.user;

import model.account.Account;
import model.account.CashAccount;
import model.exception.InsufficientFundsException;
import model.portfolio.Portfolio;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a user of the stock trading simulator.
 * <p>
 * A User aggregates a CashAccount for holding funds and a Portfolio
 * for tracking stock holdings. It provides high-level methods for
 * financial operations that delegate to the underlying account
 * and portfolio, acting as the primary aggregate root in the
 * domain model.
 * <p>
 * Encapsulation is strict: all fields are private, and mutators
 * enforce domain invariants such as non-negative balances and
 * sufficient-funds checks.
 */
public class User {

    private final long id;
    private final String username;
    private String passwordHash;
    private final Account account;
    private final Portfolio portfolio;
    private final LocalDateTime createdAt;

    /**
     * Constructs a User with the specified attributes.
     * <p>
     * The user is automatically assigned a CashAccount with the
     * default starting balance and an empty Portfolio.
     *
     * @param id            the unique user identifier
     * @param username      the unique username
     * @param passwordHash  the hashed password
     * @throws NullPointerException     if username or passwordHash is null
     * @throws IllegalArgumentException if username is blank
     */
    public User(long id, String username, String passwordHash) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                "Username must not be null or blank");
        }
        this.id = id;
        this.username = username;
        this.passwordHash = Objects.requireNonNull(passwordHash,
            "Password hash must not be null");
        this.account = new CashAccount();
        this.portfolio = new Portfolio(id);
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructs a User with a specified initial balance.
     *
     * @param id              the unique user identifier
     * @param username        the unique username
     * @param passwordHash    the hashed password
     * @param initialBalance  the starting cash balance
     */
    public User(long id, String username, String passwordHash,
                BigDecimal initialBalance) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                "Username must not be null or blank");
        }
        this.id = id;
        this.username = username;
        this.passwordHash = Objects.requireNonNull(passwordHash,
            "Password hash must not be null");
        this.account = new CashAccount(
            Objects.requireNonNull(initialBalance,
                "Initial balance must not be null"));
        this.portfolio = new Portfolio(id);
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Returns the unique user identifier.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the hashed password.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Updates the password hash.
     *
     * @param passwordHash the new hashed password
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash,
            "Password hash must not be null");
    }

    /**
     * Returns the user's cash account.
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Returns the user's portfolio.
     */
    public Portfolio getPortfolio() {
        return portfolio;
    }

    /**
     * Returns the current cash balance in the user's account.
     */
    public BigDecimal getCashBalance() {
        return account.getBalance();
    }

    /**
     * Deposits funds into the user's cash account.
     *
     * @param amount the amount to deposit
     */
    public void deposit(BigDecimal amount) {
        account.deposit(amount);
    }

    /**
     * Withdraws funds from the user's cash account.
     *
     * @param amount the amount to withdraw
     * @throws InsufficientFundsException if the cash balance is insufficient
     */
    public void withdraw(BigDecimal amount) {
        account.withdraw(amount);
    }

    /**
     * Returns the creation timestamp.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns true if this user has sufficient cash to cover the given amount.
     *
     * @param amount the amount to check
     * @return true if the cash balance is at least the given amount
     */
    public boolean canAfford(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        return account.getBalance().compareTo(amount) >= 0;
    }

    /**
     * Returns true if this user holds at least the specified quantity
     * of the given stock symbol.
     *
     * @param symbol   the stock ticker symbol
     * @param quantity the minimum quantity to check
     * @return true if the user holds at least that many shares
     */
    public boolean holdsAtLeast(String symbol, int quantity) {
        return portfolio.getPosition(symbol)
            .map(p -> p.getQuantity() >= quantity)
            .orElse(false);
    }

    @Override
    public String toString() {
        return "User{id=" + id
            + ", username='" + username + '\''
            + ", cash=" + getCashBalance()
            + ", holdings=" + portfolio.getPositionCount() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
