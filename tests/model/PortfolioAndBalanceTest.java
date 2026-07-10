package model;

import model.account.Account;
import model.account.CashAccount;
import model.exception.InsufficientFundsException;
import model.exception.InvalidOrderException;
import model.portfolio.Portfolio;
import model.portfolio.Position;
import model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests covering portfolio calculations and cash balance management.
 */
class PortfolioAndBalanceTest {

    private User user;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        user = new User(1, "trader1", "hash123", new BigDecimal("50000.00"));
        portfolio = user.getPortfolio();
    }

    // ── Cash Balance ───────────────────────────────────────

    @Test @DisplayName("User starts with correct initial balance")
    void initialBalance() {
        assertEquals(0, new BigDecimal("50000.00").compareTo(user.getCashBalance()));
    }

    @Test @DisplayName("Deposit increases cash balance")
    void deposit() {
        user.deposit(new BigDecimal("1000.00"));
        assertEquals(0, new BigDecimal("51000.00").compareTo(user.getCashBalance()));
    }

    @Test @DisplayName("Withdraw decreases cash balance")
    void withdraw() {
        user.withdraw(new BigDecimal("10000.00"));
        assertEquals(0, new BigDecimal("40000.00").compareTo(user.getCashBalance()));
    }

    @Test @DisplayName("Withdraw more than balance throws InsufficientFundsException")
    void withdrawInsufficient() {
        assertThrows(InsufficientFundsException.class,
            () -> user.withdraw(new BigDecimal("999999.00")));
    }

    @Test @DisplayName("Withdraw negative amount throws IllegalArgumentException")
    void withdrawNegative() {
        assertThrows(IllegalArgumentException.class,
            () -> user.withdraw(new BigDecimal("-100")));
    }

    @Test @DisplayName("canAfford returns true when sufficient funds")
    void canAffordTrue() {
        assertTrue(user.canAfford(new BigDecimal("30000.00")));
    }

    @Test @DisplayName("canAfford returns false when insufficient funds")
    void canAffordFalse() {
        assertFalse(user.canAfford(new BigDecimal("999999.00")));
    }

    @Test @DisplayName("Account with zero initial balance is valid")
    void zeroBalanceAccount() {
        Account acc = new CashAccount("USD", BigDecimal.ZERO);
        assertEquals(0, BigDecimal.ZERO.compareTo(acc.getBalance()));
    }

    @Test @DisplayName("Account rejects negative initial balance")
    void negativeInitialBalance() {
        assertThrows(IllegalArgumentException.class,
            () -> new CashAccount("USD", new BigDecimal("-100")));
    }

    // ── Portfolio Positions ─────────────────────────────────

    @Test @DisplayName("Empty portfolio has no positions")
    void emptyPortfolio() {
        assertTrue(portfolio.getPositions().isEmpty());
        assertEquals(0, portfolio.getPositionCount());
    }

    @Test @DisplayName("Adding shares creates a position")
    void addShares() {
        portfolio.addShares("AAPL", 100, new BigDecimal("150.00"));
        assertEquals(1, portfolio.getPositionCount());
        assertTrue(portfolio.holdsSymbol("AAPL"));
    }

    @Test @DisplayName("Adding shares calculates weighted average cost")
    void weightedAverageCost() {
        portfolio.addShares("AAPL", 100, new BigDecimal("100.00"));
        portfolio.addShares("AAPL", 100, new BigDecimal("200.00"));
        Position pos = portfolio.getPosition("AAPL").orElseThrow();
        assertEquals(200, pos.getQuantity());
        assertEquals(0, new BigDecimal("150.00").compareTo(pos.getAverageCost()));
    }

    @Test @DisplayName("Removing shares reduces quantity")
    void removeShares() {
        portfolio.addShares("AAPL", 100, new BigDecimal("150.00"));
        portfolio.removeShares("AAPL", 40);
        Position pos = portfolio.getPosition("AAPL").orElseThrow();
        assertEquals(60, pos.getQuantity());
    }

    @Test @DisplayName("Removing all shares removes the position")
    void removeAllShares() {
        portfolio.addShares("AAPL", 100, new BigDecimal("150.00"));
        portfolio.removeShares("AAPL", 100);
        assertFalse(portfolio.holdsSymbol("AAPL"));
    }

    @Test @DisplayName("Removing more shares than held throws InvalidOrderException")
    void removeTooManyShares() {
        portfolio.addShares("AAPL", 10, new BigDecimal("150.00"));
        assertThrows(InvalidOrderException.class,
            () -> portfolio.removeShares("AAPL", 20));
    }

    @Test @DisplayName("Removing from empty portfolio throws InvalidOrderException")
    void removeFromEmpty() {
        assertThrows(InvalidOrderException.class,
            () -> portfolio.removeShares("AAPL", 1));
    }

    // ── P&L Calculations ────────────────────────────────────

    @Test @DisplayName("Unrealised P&L is positive when price rises")
    void unrealisedProfit() {
        portfolio.addShares("AAPL", 100, new BigDecimal("150.00"));
        Position pos = portfolio.getPosition("AAPL").orElseThrow();
        BigDecimal pnl = pos.getUnrealisedPnl(new BigDecimal("200.00"));
        assertEquals(0, new BigDecimal("5000.00").compareTo(pnl));
    }

    @Test @DisplayName("Unrealised P&L is negative when price falls")
    void unrealisedLoss() {
        portfolio.addShares("AAPL", 100, new BigDecimal("150.00"));
        Position pos = portfolio.getPosition("AAPL").orElseThrow();
        BigDecimal pnl = pos.getUnrealisedPnl(new BigDecimal("100.00"));
        assertEquals(0, new BigDecimal("-5000.00").compareTo(pnl));
    }

    @Test @DisplayName("Market value equals quantity * current price")
    void marketValue() {
        portfolio.addShares("AAPL", 50, new BigDecimal("100.00"));
        Position pos = portfolio.getPosition("AAPL").orElseThrow();
        assertEquals(0, new BigDecimal("7500.00").compareTo(
            pos.getMarketValue(new BigDecimal("150.00"))));
    }

    @Test @DisplayName("Portfolio summary calculates total across positions")
    void portfolioSummary() {
        portfolio.addShares("AAPL", 10, new BigDecimal("100.00"));
        portfolio.addShares("GOOG", 5, new BigDecimal("200.00"));
        Portfolio.PortfolioSummary summary = portfolio.getSummary(sym -> {
            if ("AAPL".equals(sym)) return new BigDecimal("150.00");
            return new BigDecimal("250.00");
        });
        assertEquals(0, new BigDecimal("2750.00").compareTo(summary.getTotalValue()));
    }

    @Test @DisplayName("Multiple buys of same stock are tracked in one position")
    void multipleBuysSameStock() {
        portfolio.addShares("AAPL", 50, new BigDecimal("100.00"));
        portfolio.addShares("AAPL", 50, new BigDecimal("150.00"));
        portfolio.addShares("GOOG", 10, new BigDecimal("500.00"));
        assertEquals(2, portfolio.getPositionCount());
        assertEquals(100, portfolio.getPosition("AAPL").orElseThrow().getQuantity());
    }

    @Test @DisplayName("holdsAtLeast checks share count correctly")
    void holdsAtLeast() {
        portfolio.addShares("AAPL", 100, new BigDecimal("150.00"));
        assertTrue(user.holdsAtLeast("AAPL", 50));
        assertTrue(user.holdsAtLeast("AAPL", 100));
        assertFalse(user.holdsAtLeast("AAPL", 101));
        assertFalse(user.holdsAtLeast("GOOG", 1));
    }
}
