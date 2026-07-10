# Installation Guide

## Prerequisites

| Component | Minimum Version | Notes |
|---|---|---|
| Java Development Kit | 17 | Amazon Corretto, OpenJDK, or Oracle JDK |
| Apache Maven | 3.8 | Or use the bundled Maven wrapper |
| Git | 2.30 | Optional, for version control |

Verify your installation:

```bash
java --version
mvn --version
```

## Build & Run

### 1. Compile the project

```bash
cd stock-trading-simulator
mvn clean compile
```

### 2. Run tests

```bash
mvn test
```

Expected output: all tests pass (build success).

### 3. Launch the desktop application

```bash
mvn javafx:run
```

The application window opens with the login screen.

### 4. Package as JAR

```bash
mvn package
java --module-path target/classes -jar target/stock-trading-simulator-1.0.0.jar
```

## First Run

1. Launch the application
2. Click "Register" to create an account (username + password)
3. Log in with your credentials
4. Market data loads automatically from the `data/` directory
5. Start trading — browse stocks, buy/sell, track your portfolio

## Troubleshooting

| Problem | Solution |
|---|---|
| JavaFX not found | Ensure Java 17+ is the default JDK. Run `java --version`. |
| Port already in use | No network ports used — this is a local desktop app. |
| Database errors | Delete `stock_trading.db` and restart the app. |
| Maven build fails | Check `mvn --version`. Try `mvn clean compile -U`. |
| No stocks in market | Verify `data/*.csv` files exist with OHLCV format. |

## IDE Setup

### IntelliJ IDEA
1. File → Open → select project root
2. Auto-import Maven dependencies
3. Run `StockTradingApp.main()` or use Maven panel → `javafx:run`

### VS Code
1. Install "Extension Pack for Java" and "Maven for Java"
2. Open project root
3. Run via Maven panel or terminal: `mvn javafx:run`
