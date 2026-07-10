# Feature Reference

## Core Trading

### Market Orders
- **Buy Market**: Purchases shares immediately at the current market price.
- **Sell Market**: Sells held shares immediately at the current market price.
- Validation: cash balance check (buy), share quantity check (sell).
- Execution: immediate deduction/credit, position updated, trade recorded.

### Limit Orders
- **Buy Limit**: Places a pending order that fills when the market price falls to or below the specified limit.
- **Sell Limit**: Places a pending order that fills when the market price rises to or above the specified limit.
- Limit orders remain in the order book until filled or cancelled.
- Processing happens on each simulation tick.

### Stop-Loss Orders
- **Sell Stop**: Triggers a market sell when the price falls to or below the stop price.
- **Buy Stop**: Triggers a market buy when the price rises to or above the stop price.
- Once triggered, executes at the prevailing market price.

## Portfolio Management

- **Holdings**: List of positions grouped by stock symbol.
- **Average Cost**: Weighted average cost basis across all purchases.
- **Market Value**: Current value of each position (quantity × current price).
- **Unrealised P&L**: Paper profit or loss per position and in total.
- **Portfolio Summary**: Aggregate cash + holdings value, total P&L.

## Account

- **Registration**: Create account with username and password.
- **Authentication**: Login with SHA-256 hashed password verification.
- **Cash Balance**: Deposit/withdraw tracked via immutable Transaction records.
- **Multi-User**: Each user has isolated portfolio and trade history.

## User Interface

### Login Screen
- Sign in with existing credentials or register a new account.
- Password validation (minimum 4 characters).
- Username uniqueness check on registration.

### Dashboard
- Cash balance, portfolio value, unrealised P&L, stock count cards.
- Performance chart placeholder (extensible).

### Market Watch
- Scrollable table of all available stocks.
- Columns: Symbol, Company, Sector, Current Price.
- Double-click any stock to open the trading dialog.

### Portfolio View
- Holdings table with symbol, shares, average cost, market value, P&L.
- Colour-coded P&L (green for profit, red for loss).
- Portfolio summary cards.

### Trade Dialog
- Buy/Sell toggle.
- Order type selector (Market, Limit, Stop Loss).
- Quantity input with validation.
- Limit/stop price input (for non-market orders).
- Success/failure feedback.

### Trade History
- Complete list of all executed trades.
- Columns: Date, Symbol, Type, Shares, Price, Total.

## Persistence

- All data stored in a local SQLite database (`stock_trading.db`).
- Tables: `users`, `portfolios`, `orders`, `trade_history`.
- Data persists across application restarts.
- Schema initialises automatically on first run.
