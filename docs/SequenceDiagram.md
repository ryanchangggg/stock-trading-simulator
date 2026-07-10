# Sequence Diagrams

## Market Buy Order Flow

```plantuml
@startuml
actor "User" as USER
participant "TradingService" as SERVICE
participant "TradingEngine" as ENGINE
participant "OrderValidator" as VALIDATOR
participant "MarketOrderExecutor" as EXECUTOR
participant "OrderRepository" as ORDER_REPO
participant "TradeRepository" as TRADE_REPO
participant "UserRepository" as USER_REPO

USER -> SERVICE: buyMarket(session, "AAPL", 100)
SERVICE -> SERVICE: create BuyOrder(id, userId, "AAPL", 100, price, MARKET)
SERVICE -> ENGINE: placeOrder(order, user, market)

ENGINE -> VALIDATOR: validate(order, user, market)
VALIDATOR -> VALIDATOR: check: cash >= 100 * price
VALIDATOR -> VALIDATOR: check: stock exists in market
VALIDATOR --> ENGINE: OK

ENGINE -> EXECUTOR: execute(order, user, market, tradeId)
EXECUTOR -> USER: withdraw(100 * price)
EXECUTOR -> USER.getPortfolio(): addShares("AAPL", 100, price)
EXECUTOR -> order: fill()
EXECUTOR --> ENGINE: ExecutionResult.filled(trade)
ENGINE -> ORDER_REPO: save(order)
ENGINE -> TRADE_REPO: save(trade)
ENGINE -> USER_REPO: save(user)
ENGINE --> SERVICE: ExecutionResult{success}

SERVICE --> USER: Result.success(trade)
@enduml
```

## Limit Sell Order Flow

```plantuml
@startuml
actor "User" as USER
participant "TradingService" as SERVICE
participant "TradingEngine" as ENGINE
participant "OrderBook" as BOOK
participant "OrderRepository" as ORDER_REPO
participant "UserRepository" as USER_REPO
participant "LimitOrderExecutor" as EXECUTOR
participant "Market" as MARKET

== Placement ==
USER -> SERVICE: sellLimit(session, "AAPL", 50, $200.00)
SERVICE -> SERVICE: create SellOrder(id, userId, "AAPL", 50, $200, LIMIT)
SERVICE -> ENGINE: placeOrder(order, user, market)
ENGINE -> EXECUTOR: execute(order, user, market, tradeId)
EXECUTOR --> ENGINE: ExecutionResult.pending(order)
ENGINE -> BOOK: add(order)
ENGINE -> ORDER_REPO: save(order)
ENGINE --> SERVICE: ExecutionResult{pending}
SERVICE --> USER: Result.success(order)

== Tick Processing ==
loop each simulation tick
    MARKET -> MARKET: price tick → $199.80
    ENGINE -> ENGINE: processPendingOrders(market)
    ENGINE -> BOOK: getAllPending()
    BOOK --> ENGINE: [Order(id=15, AAPL, SELL, $200)]
    ENGINE -> EXECUTOR: execute(order, user, market, tradeId)
    EXECUTOR -> MARKET: getCurrentPrice("AAPL")
    MARKET --> EXECUTOR: $199.80
    EXECUTOR -> EXECUTOR: $199.80 < $200.00 → no fill
    EXECUTOR --> ENGINE: ExecutionResult.pending(order)
    
    MARKET -> MARKET: price tick → $200.45
    ENGINE -> ENGINE: processPendingOrders(market)
    ENGINE -> BOOK: getAllPending()
    BOOK --> ENGINE: [Order(id=15, AAPL, SELL, $200)]
    ENGINE -> EXECUTOR: execute(order, user, market, tradeId)
    EXECUTOR -> MARKET: getCurrentPrice("AAPL")
    MARKET --> EXECUTOR: $200.45
    EXECUTOR -> EXECUTOR: $200.45 >= $200.00 → FILL
    EXECUTOR -> USER.getPortfolio(): removeShares("AAPL", 50)
    EXECUTOR -> USER: deposit($10,022.50)
    EXECUTOR -> order: fill()
    EXECUTOR --> ENGINE: ExecutionResult.filled(trade)
    ENGINE -> BOOK: remove(order.id)
    ENGINE -> ORDER_REPO: save(order)
    ENGINE -> TRADE_REPO: save(trade)
    ENGINE -> USER_REPO: save(user)
    ENGINE --> ENGINE: record result
end
@enduml
```
