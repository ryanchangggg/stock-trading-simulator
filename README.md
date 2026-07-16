# Stock Trading Simulator

A desktop stock trading simulator built for an Object-Oriented Programming project. Practice equity trading with virtual currency in a risk-free simulation environment.

## Features

- **Virtual trading** with $100,000 starting balance
- **Market orders** — buy/sell instantly at current prices
- **Limit orders** — buy/sell only at a specified price or better
- **Stop-loss orders** — automatically sell when price drops to a threshold
- **Real-time simulation** driven by historical market data (CSV)
- **Portfolio tracking** with average cost basis and unrealised P&L
- **Trade history** with complete audit trail
- **SQLite persistence** — all data survives restarts
- **Multi-user support** — isolated portfolios per user

## Architecture

OOP principles demonstrated throughout:

| Principle | How It's Applied |
|---|---|
| **Encapsulation** | All model fields are private; state changed only through behavioural methods |
| **Inheritance** | `Order` → `BuyOrder`, `SellOrder`, `StopLossOrder`; `Account` → `CashAccount` |
| **Polymorphism** | `OrderExecutionStrategy` strategies for market/limit/stop-loss orders |
| **Abstraction** | Repository interfaces, strategy interfaces, abstract model classes |
| **SRP** | Each class has one responsibility |
| **OCP** | New order types by implementing `OrderExecutionStrategy` |
| **DIP** | Engine depends on repository interfaces, not SQLite directly |

## Design Patterns

| Pattern | Usage |
|---|---|
| **Strategy** | Order execution strategies, price simulation modes |
| **Observer** | Simulation engine notifies portfolio/order watchers on price ticks |
| **Factory** | `RepositoryFactory` creates all persistence implementations |
| **Facade** | `TradingEngine` orchestrates validation + execution + persistence |
| **Template Method** | `Order.validate()` defines skeleton, subclasses fill details |
| **Repository** | Interfaces abstract the data store from business logic |
| **Singleton** | `DatabaseConnection` manages shared SQLite connection |

## Project Structure

```
src/main/java/com/stocktrader/
├── model/          # Domain entities (User, Order, Portfolio, Stock, Trade, etc.)
├── repository/     # Persistence interfaces + SQLite implementations
├── engine/         # Trading engine, order book, execution strategies
├── service/        # Service layer between UI and engine/repositories
└── ui/             # JavaFX desktop application

src/test/java/com/stocktrader/
├── engine/         # TradingEngine tests with mock repositories
├── model/          # Portfolio, balance, and calculation tests
└── service/        # Service layer tests with mock engine
```

## Quick Start

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Clone and build
cd stock-trading-simulator
mvn clean compile

# Run tests
mvn test

# Launch desktop application
mvn javafx:run
```

## Disclaimer

This repository is intended for educational and research purposes only.

The software, code, models, and any accompanying materials do not constitute investment, financial, legal, or tax advice.

All investment decisions are made solely at the user's own risk. The author assumes no responsibility or liability for any financial losses or damages arising from the use of this project.

Past performance, backtesting results, and simulated trading outcomes do not guarantee future performance.

This software is provided "AS IS", without warranty of any kind.

---

*Built with Java 17, JavaFX 21, SQLite, Jackson, JUnit 5, and Mockito.*
