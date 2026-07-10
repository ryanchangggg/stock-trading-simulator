# Design Patterns

## Overview

This document describes the design patterns used in the Stock Trading Simulator. Each pattern is explained with its intent, participants, and specific application in the codebase.

---

## 1. Singleton

| **Aspect** | **Description** |
|---|---|
| **Intent** | Ensure a single instance of a class and provide a global access point |
| **Location** | `util.DatabaseConnection` |
| **Participants** | `getInstance()` static factory, private constructor |
| **Rationale** | The entire application shares one SQLite connection; multiplexing multiple connections to the same file would cause locking errors |

---

## 2. Factory Method

| **Aspect** | **Description** |
|---|---|
| **Intent** | Delegate object creation to subclasses or static factory methods |
| **Location** | `factory.RepositoryFactory` |
| **Participants** | `RepositoryFactory` acts as the factory; creates `SqliteUserRepository`, `SqlitePortfolioRepository`, `SqliteOrderRepository`, `SqliteTradeHistoryRepository` |
| **Rationale** | Centralizes persistence-layer construction; adding a new repository type requires only changes to the factory |

---

## 3. Strategy

| **Aspect** | **Description** |
|---|---|
| **Intent** | Encapsulate interchangeable algorithms behind a common interface |
| **Location** | `strategy.OrderExecutionStrategy` with `MarketOrderExecutor`, `LimitOrderExecutor`, `StopLossExecutor` |
| **Participants** | `Context`: `service.TradingEngine`, `Strategy`: `OrderExecutionStrategy`, `ConcreteStrategy`: `MarketOrderExecutor`, `LimitOrderExecutor`, `StopLossExecutor` |
| **Rationale** | The engine routes orders to the appropriate executor without knowing execution details. Adding `TrailingStopExecutor` requires zero changes to `TradingEngine` |

---

## 4. Observer

| **Aspect** | **Description** |
|---|---|
| **Intent** | Define a one-to-many dependency so that when one object changes state, all dependents are notified |
| **Location** | `observer.TradeObserver` interface (extensible) |
| **Participants** | `Subject`: `service.TradingEngine`, `Observer`: `observer.TradeObserver` |
| **Rationale** | Decouples the trading engine from portfolio watchers, UI updates, and logging. Currently the interface is defined; concrete observers can be registered without engine changes |

---

## 5. Repository

| **Aspect** | **Description** |
|---|---|
| **Intent** | Mediate between domain and data mapping layers using an interface |
| **Location** | `repository.UserRepository`, `repository.PortfolioRepository`, `repository.OrderRepository`, `repository.TradeRepository` (interfaces); `repository.Sqlite*Repository` (implementations) |
| **Participants** | `Repository` interfaces in `repository/`; concrete implementations in `repository/` (moved from `sqlite/`) |
| **Rationale** | The engine and services depend only on repository interfaces — zero coupling to SQLite or any other data store. Test doubles (in-memory mocks) are trivially substitutable |

---

## 6. Template Method

| **Aspect** | **Description** |
|---|---|
| **Intent** | Define the skeleton of an algorithm in a base class, deferring steps to subclasses |
| **Location** | `model.order.Order` with abstract methods `validate()` and `getDescription()` |
| **Participants** | `AbstractClass`: `Order`, `ConcreteClass`: `BuyOrder`, `SellOrder`, `StopLossOrder` |
| **Rationale** | The order lifecycle (creation, validation, fill, cancel) is defined once in `Order`; subclasses override only the type-specific behaviour |

---

## 7. Command

| **Aspect** | **Description** |
|---|---|
| **Intent** | Encapsulate a request as an object |
| **Location** | `service.TradingEngine` with `ExecutionResult` as the result DTO |
| **Participants** | `Command`: Each order type encapsulates its execution logic via the Strategy pattern; `Receivers`: `model.user.User`, `model.market.Market` |
| **Rationale** | Decouples the invoker (UI / CLI) from execution logic. Supports undo/redo as a future extension by storing a command history stack |

---

## 8. Facade

| **Aspect** | **Description** |
|---|---|
| **Intent** | Provide a unified interface to a set of interfaces in a subsystem |
| **Location** | `service.TradingEngine`, `service.TradingService` |
| **Participants** | `Facade`: `TradingService` exposes `buyMarket()`, `sellLimit()`, `cancelOrder()`, etc.; Subsystem: `TradingEngine`, repositories, `OrderValidator` |
| **Rationale** | The UI calls a single clean API; it never touches the engine, validator, or repositories directly |

---

## 9. MVC (Architectural)

| **Aspect** | **Description** |
|---|---|
| **Intent** | Separate concerns into Model, View, and Controller |
| **Location** | `model/` (Model), `ui/` (View), `service/` (Controller) |
| **Participants** | `Model`: Entity objects in `model/`, `View`: JavaFX views in `ui/`, `Controller`: Services in `service/` |
| **Rationale** | Each layer has a single concern; swapping the CLI for a REST API would replace only the View layer |
