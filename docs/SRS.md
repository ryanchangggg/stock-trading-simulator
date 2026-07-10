# Software Requirements Specification

## Stock Trading Simulator

| **Field**        | **Details**                                      |
| ---------------- | ------------------------------------------------ |
| **Project Name** | Stock Trading Simulator                          |
| **Version**      | 1.0                                              |
| **Author**       | —                                                |
| **Course**       | Object-Oriented Programming (University Project) |
| **Date**         | July 2026                                        |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Scope](#2-scope)
3. [System Constraints](#3-system-constraints)
4. [User Stories](#4-user-stories)
5. [Functional Requirements](#5-functional-requirements)
6. [Non-Functional Requirements](#6-non-functional-requirements)
7. [Use Cases](#7-use-cases)
8. [Object-Oriented Design Goals](#8-object-oriented-design-goals)
9. [Design Pattern Mapping](#9-design-pattern-mapping)
10. [Future Extensions](#10-future-extensions)
11. [Glossary](#11-glossary)

---

## 1. Introduction

### 1.1 Purpose

The Stock Trading Simulator is a desktop application that allows users to practice equity trading with virtual currency in a risk-free environment. Users can buy and sell shares of publicly traded companies using historical and simulated real-time price data, track their portfolio performance, and analyse trading history — all without committing real capital.

The application is designed as a university capstone project for an Object-Oriented Programming course. Its architecture deliberately demonstrates encapsulation, inheritance, polymorphism, abstraction, the SOLID principles, and a curated set of software design patterns.

### 1.2 Intended Audience

- **Instructor / Evaluator** — assesses the project against OOP and design-pattern criteria.
- **Developer** — understands requirements before implementation.
- **Tester** — derives test cases from documented requirements and use cases.
- **Student (user)** — the end-user who will trade simulated stocks.

### 1.3 Product Overview

The simulator operates in two modes:

- **Real-time Simulation** — Prices tick forward from a loaded data file at a configurable speed, mimicking a live market feed.
- **Historical / Backtest Mode** — A user can navigate through a date range and observe how a strategy would have performed.

Every user starts with a virtual cash balance (default $100,000). They place market or limit orders, which the system matches against the current or target price. A portfolio tracker reflects holdings, average cost, and unrealised gain/loss in real time.

The system persists user accounts, portfolios, orders, and trade history in a local SQLite database. Market data is loaded from CSV files containing daily OHLCV (Open, High, Low, Close, Volume) records for a selection of US equities.

---

## 2. Scope

### 2.1 In Scope (v1.0)

- **User management** — account creation, authentication, session handling.
- **Virtual portfolio management** — buy/sell equities, track holdings, calculate cost basis.
- **Order management** — place market and limit orders; view pending, filled, and cancelled orders.
- **Trade history** — complete ledger of all transactions with timestamps.
- **Market data engine** — CSV-driven price simulation forward and backward through time.
- **Portfolio analytics** — P&L, asset allocation, portfolio value over time.
- **Simulation controls** — speed multiplier, pause/resume, jump to date.
- **Data persistence** — local SQLite storage for users, portfolios, orders, and trades.
- **Multi-user isolation** — each user sees only their own portfolio and orders.

### 2.2 Not in Scope (v1.0)

- Real brokerage API integration or live market data feeds.
- Multi-user collaboration, leaderboards, or social features.
- Margin trading, short selling, or options/futures/forex instruments.
- Currency conversion for non-USD instruments.
- Real-time WebSocket streaming from an exchange.
- Mobile or web frontend (desktop-only; terminal or simple GUI).
- Statistical modelling, machine learning, or automated trading bots.
- Two-factor authentication or single sign-on.

---

## 3. System Constraints

### 3.1 Technical Constraints

| Constraint             | Specification                                          |
| ---------------------- | ------------------------------------------------------ |
| **Language**           | Java 17+                                               |
| **Build tool**         | Manual compilation (javac) or simple build script      |
| **Database**           | SQLite via JDBC                                        |
| **Data format**        | CSV for market data (OHLCV)                            |
| **JSON library**       | Jackson (databind, core, annotations)                  |
| **UI**                 | Command-line interface with clear text-based menus     |
| **Supported OS**       | macOS (primary), Linux, Windows (JVM compatible)       |
| **Persistence**        | Local file-based (SQLite .db + CSV in data/ directory) |

### 3.2 Domain Constraints

- All monetary values are in USD only.
- Prices are limited to two decimal places internally.
- Order quantity must be a positive integer (no fractional shares in v1.0).
- A user cannot sell more shares than they hold in their portfolio (no shorting).
- A user cannot place a buy order exceeding their available cash balance.
- A single user session is single-threaded; no concurrent order execution.

---

## 4. User Stories

| ID     | Story                                                                                                                                  |
| ------ | -------------------------------------------------------------------------------------------------------------------------------------- |
| US-01  | As a new user, I want to register an account so that I can start trading with my own virtual portfolio.                                |
| US-02  | As a registered user, I want to log in so that I can access my portfolio and trading history.                                          |
| US-03  | As a trader, I want to view a list of available stocks with their latest prices so that I can decide what to buy.                      |
| US-04  | As a trader, I want to buy shares of a stock (market order) at the current price so that I can add it to my portfolio.                 |
| US-05  | As a trader, I want to buy shares with a limit price so that the order fills only when the price reaches my target.                    |
| US-06  | As a trader, I want to sell shares I already own (market order) so that I can realise gains or cut losses.                             |
| US-07  | As a trader, I want to place a limit sell order so that I can lock in a target profit automatically.                                   |
| US-08  | As a trader, I want to see my current holdings (symbol, quantity, avg cost, current value) so that I can assess my portfolio.          |
| US-09  | As a trader, I want to view my order history so that I can track pending and completed transactions.                                   |
| US-10  | As a trader, I want to cancel a pending limit order so that I can change my mind before it fills.                                      |
| US-11  | As a trader, I want to see my total portfolio value and unrealised P&L so that I can evaluate performance.                             |
| US-12  | As a trader, I want to control simulation speed (pause, resume, fast-forward) so that I can trade at a comfortable pace.               |
| US-13  | As a trader, I want to view a price chart for a stock so that I can identify trends before trading.                                    |
| US-14  | As a trader, I want to see my trade history with timestamps and realised P&L so that I can review past decisions.                      |
| US-15  | As a trader, I want to jump to a specific date in the simulation so that I can test strategies against historical events.              |

---

## 5. Functional Requirements

### FR-1 — User Management

| ID     | Requirement                                                                     |
| ------ | ------------------------------------------------------------------------------- |
| FR-1.1 | The system shall allow a new user to register with a unique username and password. |
| FR-1.2 | The system shall securely hash passwords before storing them.                    |
| FR-1.3 | The system shall allow a registered user to authenticate with username/password. |
| FR-1.4 | The system shall assign each new user a starting cash balance of $100,000.       |
| FR-1.5 | The system shall support exactly one active session per user at a time.          |

### FR-2 — Market Data

| ID     | Requirement                                                                       |
| ------ | --------------------------------------------------------------------------------- |
| FR-2.1 | The system shall load stock data from CSV files in the `data/` directory.          |
| FR-2.2 | Each CSV file shall contain columns: Date, Open, High, Low, Close, Volume.        |
| FR-2.3 | The system shall provide a real-time simulation mode that advances price ticks forward. |
| FR-2.4 | The system shall provide a historical mode that allows navigating by date.          |
| FR-2.5 | The simulation speed multiplier shall be configurable (1x, 2x, 5x, 10x).          |
| FR-2.6 | The system shall display the current simulation date and time.                     |

### FR-3 — Order Management

| ID     | Requirement                                                                  |
| ------ | ---------------------------------------------------------------------------- |
| FR-3.1 | The system shall support market buy and market sell orders.                  |
| FR-3.2 | The system shall support limit buy and limit sell orders.                    |
| FR-3.3 | A market order shall execute immediately at the current price.               |
| FR-3.4 | A limit order shall remain pending until the market price reaches the limit. |
| FR-3.5 | The system shall allow the user to cancel a pending limit order.             |
| FR-3.6 | The system shall validate that a buy order does not exceed available cash.   |
| FR-3.7 | The system shall validate that a sell order does not exceed held shares.     |
| FR-3.8 | Each filled order shall generate a corresponding trade history record.       |

### FR-4 — Portfolio

| ID     | Requirement                                                                          |
| ------ | ------------------------------------------------------------------------------------ |
| FR-4.1 | The system shall display the user's current holdings grouped by stock symbol.       |
| FR-4.2 | Each holding shall show: symbol, quantity, average cost, current price, total value. |
| FR-4.3 | The system shall calculate and display total portfolio value (cash + holdings).      |
| FR-4.4 | The system shall calculate and display unrealised P&L per holding and in total.      |
| FR-4.5 | The system shall update portfolio values each time a price tick occurs.              |

### FR-5 — Trade History

| ID     | Requirement                                                                                             |
| ------ | ------------------------------------------------------------------------------------------------------- |
| FR-5.1 | The system shall record every completed trade with: symbol, type (BUY/SELL), quantity, price, total, timestamp. |
| FR-5.2 | The system shall display the user's trade history sorted by date (newest first).                        |
| FR-5.3 | The system shall calculate and display realised P&L for completed sell trades.                          |

### FR-6 — Reporting

| ID     | Requirement                                                                                    |
| ------ | ---------------------------------------------------------------------------------------------- |
| FR-6.1 | The system shall display a summary dashboard showing cash balance, portfolio value, and P&L.   |
| FR-6.2 | The system shall support exporting trade history as CSV or JSON.                               |

---

## 6. Non-Functional Requirements

### 6.1 Usability

| ID    | Requirement                                                                                    |
| ----- | ---------------------------------------------------------------------------------------------- |
| NFR-1 | The CLI shall present clear, well-formatted menus with numbered options.                        |
| NFR-2 | All monetary values shall be formatted with two decimal places and a currency symbol.            |
| NFR-3 | Error messages shall be descriptive and indicate how to resolve the issue.                       |
| NFR-4 | The system shall display real-time feedback after every user action (order placed, filled, etc.).|

### 6.2 Performance

| ID    | Requirement                                                                              |
| ----- | ---------------------------------------------------------------------------------------- |
| NFR-5 | A market order shall be processed and reported within 500 ms of submission.               |
| NFR-6 | The simulation engine shall support tick intervals as low as 100 ms without degradation.  |
| NFR-7 | Loading 20+ CSV files at startup shall complete within 5 seconds.                         |
| NFR-8 | The application shall not consume more than 256 MB of heap memory during normal operation.|

### 6.3 Reliability

| ID    | Requirement                                                                                         |
| ----- | --------------------------------------------------------------------------------------------------- |
| NFR-9 | The system shall commit all financial transactions atomically (all-or-nothing to DB).                |
| NFR-10 | In the event of a crash, committed trades shall not be lost (durability within the same session).   |
| NFR-11 | The system shall validate all user inputs before processing to prevent invalid state transitions.    |

### 6.4 Maintainability

| ID     | Requirement                                                                                    |
| ------ | ---------------------------------------------------------------------------------------------- |
| NFR-12 | The codebase shall be organised into packages by domain responsibility. |
| NFR-13 | Each class shall have a single, clearly documented responsibility (SRP).                        |
| NFR-14 | Adding a new order type shall require no changes to existing order-processing logic (OCP).      |
| NFR-15 | Database schema changes shall be isolated behind a repository abstraction layer.                |

### 6.5 Security

| ID     | Requirement                                                                                       |
| ------ | ------------------------------------------------------------------------------------------------- |
| NFR-16 | Passwords shall be stored as salted SHA-256 or bcrypt hashes — never in plain text.                |
| NFR-17 | Users shall only access their own portfolio, orders, and trade history.                            |
| NFR-18 | The system shall sanitise all user input to prevent injection attacks against the SQLite database. |

---

## 7. Use Cases

### UC-01: Register Account

| Section       | Description                                         |
| ------------- | --------------------------------------------------- |
| **Actors**    | Unregistered User                                   |
| **Precondition**  | None                                            |
| **Trigger**   | User selects "Register" from the main menu.          |
| **Main Flow** | 1. System prompts for username and password.         |
|               | 2. User enters credentials.                          |
|               | 3. System validates uniqueness of the username.      |
|               | 4. System hashes the password.                       |
|               | 5. System creates the user record in the database with a $100,000 starting balance. |
|               | 6. System confirms successful registration.          |
| **Alternate** | 3a. Username already taken — system rejects and prompts for a different name. |
| **Postcondition** | A new user account exists in the database.       |

### UC-02: Log In

| Section       | Description                                                   |
| ------------- | ------------------------------------------------------------- |
| **Actors**    | Registered User                                               |
| **Precondition**  | Account exists.                                            |
| **Trigger**   | User selects "Log In" from the main menu.                     |
| **Main Flow** | 1. System prompts for username and password.                   |
|               | 2. User enters credentials.                                    |
|               | 3. System verifies the password hash.                          |
|               | 4. System loads the user's portfolio and trade history.        |
|               | 5. System displays the main trading dashboard.                 |
| **Alternate** | 3a. Invalid credentials — system rejects with a generic error and returns to the main menu. |
| **Postcondition** | User is authenticated and the trading session has started. |

### UC-03: Place Market Order (Buy)

| Section       | Description                                                                                 |
| ------------- | ------------------------------------------------------------------------------------------- |
| **Actors**    | Authenticated User                                                                          |
| **Precondition**  | User is logged in. Simulation is running or paused with a valid market price available.   |
| **Trigger**   | User selects "Buy (Market)" from the trading menu.                                          |
| **Main Flow** | 1. System shows list of available stocks and current prices.                                |
|               | 2. User selects a stock symbol.                                                             |
|               | 3. User enters the quantity of shares to buy.                                               |
|               | 4. System validates the total cost ≤ available cash balance.                                |
|               | 5. System deducts cash from the user's balance.                                             |
|               | 6. System adds the shares to the user's portfolio (or adjusts average cost if already held).|
|               | 7. System records the trade in TradeHistory.                                                |
|               | 8. System displays confirmation and updated portfolio summary.                              |
| **Alternate** | 4a. Insufficient funds — system rejects with an error and returns to the trading menu.      |
| **Postcondition** | User holds the purchased shares; cash balance is reduced.                              |

### UC-04: Place Limit Order (Sell)

| Section       | Description                                                                                     |
| ------------- | ----------------------------------------------------------------------------------------------- |
| **Actors**    | Authenticated User                                                                              |
| **Precondition**  | User holds at least one share of the selected stock.                                          |
| **Trigger**   | User selects "Sell (Limit)" from the trading menu.                                              |
| **Main Flow** | 1. System shows the user's holdings for the selected stock.                                     |
|               | 2. User enters the quantity and limit price.                                                    |
|               | 3. System validates quantity ≤ shares held.                                                     |
|               | 4. System creates a PENDING order in the Orders table.                                          |
|               | 5. System monitors the simulated price.                                                         |
|               | 6. When the market price reaches or exceeds the limit price, the system fills the order.        |
|               | 7. System credits the user's cash balance and deducts shares.                                   |
|               | 8. System records the trade in TradeHistory with realised P&L.                                  |
| **Alternate** | 3a. Insufficient shares — system rejects with error.                                            |
|               | 6a. User cancels the order before fill — system sets status to CANCELLED.                       |
| **Postcondition** | If filled, user's cash increases; shares are removed. If cancelled, no state change.       |

### UC-05: View Portfolio

| Section       | Description                                                                           |
| ------------- | ------------------------------------------------------------------------------------- |
| **Actors**    | Authenticated User                                                                    |
| **Precondition**  | User is logged in.                                                                 |
| **Trigger**   | User selects "View Portfolio" from the dashboard.                                     |
| **Main Flow** | 1. System retrieves all holdings for the user.                                         |
|               | 2. For each holding, system looks up the current market price.                         |
|               | 3. System calculates: total value per holding, average cost, unrealised P&L.           |
|               | 4. System displays a table of holdings and a summary line. |
| **Postcondition** | Portfolio information is displayed to the user.                                 |

### UC-06: Cancel Pending Order

| Section       | Description                                                                  |
| ------------- | ---------------------------------------------------------------------------- |
| **Actors**    | Authenticated User                                                           |
| **Precondition**  | User has at least one PENDING limit order.                                |
| **Trigger**   | User selects "Cancel Order" from the order management screen.               |
| **Main Flow** | 1. System lists the user's pending orders with IDs.                          |
|               | 2. User selects the order ID to cancel.                                      |
|               | 3. System updates the order status to CANCELLED.                             |
|               | 4. System confirms the cancellation.                                         |
| **Postcondition** | The order is cancelled and will never be filled.                        |

### UC-07: Adjust Simulation Speed

| Section       | Description                                                                  |
| ------------- | ---------------------------------------------------------------------------- |
| **Actors**    | Authenticated User                                                           |
| **Precondition**  | Simulation is running.                                                    |
| **Trigger**   | User selects "Change Speed" from the simulation controls.                    |
| **Main Flow** | 1. System shows current speed and available options (1x, 2x, 5x, 10x).       |
|               | 2. User selects a new speed multiplier.                                      |
|               | 3. System adjusts the tick interval accordingly.                             |
|               | 4. System displays the new speed.                                            |
| **Postcondition** | Simulation ticks at the new rate.                                       |

### UC-08: View Trade History

| Section       | Description                                                                    |
| ------------- | ------------------------------------------------------------------------------ |
| **Actors**    | Authenticated User                                                             |
| **Precondition**  | User is logged in and has completed at least one trade.                     |
| **Trigger**   | User selects "Trade History" from the dashboard.                               |
| **Main Flow** | 1. System retrieves all trade history records for the user, sorted by date.    |
|               | 2. System displays a table with columns: date, symbol, type, qty, price, total, P&L. |
|               | 3. System offers an option to export as CSV.                                   |
| **Postcondition** | Trade history is displayed; optionally exported to file.                     |

---

## 8. Object-Oriented Design Goals

### 8.1 Encapsulation

- All domain model fields (e.g., `User.cashBalance`, `Portfolio.quantity`, `Order.price`) are `private` and exposed only through controlled getters.
- Mutable state is modified exclusively through well-defined behavioural methods (`deposit()`, `withdraw()`, `fill()`, `cancel()`), never via direct field access.
- The database implementation is hidden behind a repository interface; callers never touch JDBC or SQL strings.

### 8.2 Inheritance

- `Order` is an abstract base class; `MarketOrder` and `LimitOrder` extend it, overriding validation and execution logic.
- A `Stock` base class may be extended by `EquityStock` (and later `IndexStock` or `ETFStock`) if additional instrument types are added.
- UI components use a template-method pattern via an abstract `View` class; `PortfolioView`, `OrderView`, and `MarketView` implement `render()` differently.

### 8.3 Polymorphism

- The `OrderProcessor` works polymorphically — it accepts any `Order` subtype and calls `execute()` without knowing the concrete class.
- A `PriceDataSource` interface allows the simulation engine and a future live-data provider to be swapped transparently.
- The `ReportExporter` interface has `export(TradeHistory)`: `CsvExporter` and `JsonExporter` implementations differ only in format.

### 8.4 Abstraction

- Core domain concepts are modelled as interfaces and abstract classes: `Account`, `Order`, `Trade`, `MarketDataSource`.
- The database layer is abstracted behind `UserRepository`, `PortfolioRepository`, `OrderRepository`, and `TradeHistoryRepository` interfaces.
- The simulation clock is abstracted behind a `SimulationClock` interface, enabling unit tests to inject a fixed-time implementation.

### 8.5 SOLID Principles

| **Principle** | **Application**                                                                                                                      |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| **SRP**       | Every class has one reason to change. `OrderBook` manages order matching; `PortfolioService` manages holdings; `UserRepository` manages persistence. |
| **OCP**       | New order types extend `Order` without modifying `OrderProcessor`. New exporters implement `ReportExporter` without touching the reporting UI. |
| **LSP**       | `MarketOrder` and `LimitOrder` are fully substitutable for `Order` — callers depend only on the abstract contract.                     |
| **ISP**       | `PortfolioRepository` exposes only portfolio-specific methods; a user-management client depends only on `UserRepository`, not a bloated `DataAccess` interface. |
| **DIP**       | High-level `OrderService` depends on `OrderRepository` (interface), not `SqliteOrderRepository` (concrete implementation). Construction is done via dependency injection (manual or factory). |

---

## 9. Design Pattern Mapping

| **Pattern**       | **Where Used**                                                                                                      | **Rationale**                                                                                            |
| ----------------- | ------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------- |
| **Singleton**     | `SimulationEngine` — one clock instance per session manages all timing and tick dispatch.                            | Guarantees a single simulation state; prevents duplicate tick handlers.                                  |
| **Factory Method** | `OrderFactory.createOrder(type, params)` returns `MarketOrder` or `LimitOrder` based on input.                    | Encapsulates order construction; adding a new order type does not affect client code.                    |
| **Strategy**      | `PriceSimulationStrategy` interface with `ForwardSimulation` and `HistoricalNavigation` implementations.            | Lets the user switch simulation modes at runtime without conditional logic in the engine.                |
| **Observer**      | `SimulationEngine` (subject) notifies registered `PortfolioView`, `MarketView`, and `OrderWatcher` (observers) on each tick. | Decouples the simulation engine from UI components; new observers can be added without engine changes. |
| **Repository**    | `UserRepository`, `PortfolioRepository`, `OrderRepository`, `TradeHistoryRepository` (interfaces + Sqlite impl).     | Isolates persistence logic; swaps easily for testing (e.g., `InMemoryUserRepository`).                  |
| **Template Method** | `View.render()` defines an algorithm skeleton; `PortfolioView` and `OrderView` override `renderHeader()`, `renderBody()`, `renderFooter()`. | Reuses layout logic while letting subclasses customise content sections.                                 |
| **Command**       | Each user action (BuyOrder, SellOrder, CancelOrder, ExportHistory) is encapsulated as a `Command` object.            | Supports undo/redo in future extensions; decouples the invoker (menu/CLI) from execution logic.          |
| **Facade**        | `TradingFacade` exposes a simplified API (`placeBuyOrder()`, `placeSellOrder()`, `getPortfolioSummary()`) to the UI layer. | Hides the complexity of the service/repository/engine interaction tree from the presentation layer.   |
| **MVC (Architectural)** | `Model` (domain + repositories) ← `Controller` (services + facade) ← `View` (CLI screens).                    | Standard separation of concerns; testable logic independent of the user interface.                       |

---

## 10. Future Extensions

The following features are explicitly out of scope for v1.0 but are accommodated by the architecture:

| **Extension**             | **Description**                                                                             | **Pattern / Principle Supporting It**                               |
| ------------------------- | ------------------------------------------------------------------------------------------- | ------------------------------------------------------------------- |
| **Live Market Data**      | Replace CSV simulation with a real-time data feed via WebSocket or REST API.                | `PriceDataSource` interface (Strategy)                              |
| **Technical Indicators**  | SMA, EMA, RSI, Bollinger Bands displayed on price charts.                                   | `Indicator` interface (Strategy) + Observer                         |
| **Automated Trading Bot** | Users write or configure simple rules that generate orders automatically.                    | Command + Strategy + Observer                                       |
| **Dark / Light Theme**    | Toggle between UI colour schemes (if GUI is added later).                                    | `Theme` interface (Strategy)                                        |
| **Undo / Redo**           | Revert accidental orders within the same session.                                            | Command pattern (stored `CommandHistory` stack)                     |
| **Margin Trading**        | Borrow virtual cash to trade beyond the cash balance.                                        | New `MarginAccount` subclass (Inheritance)                          |
| **Short Selling**         | Sell shares not yet owned and buy them back later.                                           | New `ShortOrder` subtype + `ShortPortfolio` (Inheritance)           |
| **Multi-Currency**        | Support for non-USD stocks and currency conversion.                                          | `CurrencyConverter` (Strategy)                                      |
| **Leaderboard**           | Compare portfolio performance across users.                                                  | New `LeaderboardService` + Observer on portfolio changes            |
| **Web / GUI Frontend**    | Replace the CLI with a Java Swing/JavaFX desktop GUI, or a REST API + JavaScript frontend.  | Facade + MVC — the existing service layer remains unchanged.        |
| **Unit & Integration Tests** | Automated test suite using JUnit + Mockito.                                              | Repository pattern enables in-memory test doubles; DIP makes mocking natural. |
| **Database Migrations**   | Manage schema changes over time with versioned migration scripts.                            | `SchemaMigrator` class + version table.                             |

---

## 11. Glossary

| **Term**          | **Definition**                                                                                      |
| ----------------- | --------------------------------------------------------------------------------------------------- |
| **Ask / Offer**   | The lowest price a seller is willing to accept for a stock.                                         |
| **Bid**           | The highest price a buyer is willing to pay for a stock.                                            |
| **CLI**           | Command-Line Interface — text-based interaction via terminal.                                       |
| **Fill**          | The execution of an order at a specific price.                                                      |
| **Limit Order**   | An order to buy or sell at a specified price or better; rests in the order book until filled or cancelled. |
| **Market Order**  | An order to buy or sell immediately at the current market price.                                    |
| **OHLCV**         | Open, High, Low, Close, Volume — the standard five-value summary of a trading period.               |
| **P&L**           | Profit and Loss — the difference between the current value and the cost basis.                      |
| **Pending Order** | An order submitted but not yet filled (applicable to limit orders).                                 |
| **Portfolio**     | A collection of financial assets (cash + equities) held by a user.                                  |
| **Realised P&L**  | Profit or loss from completed sell trades.                                                          |
| **Tick**          | A single price update in the simulation (moving to the next data point in time).                     |
| **Unrealised P&L**| The paper gain or loss on still-held positions, based on the current market price.                   |

---
