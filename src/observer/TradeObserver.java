package observer;

import model.order.Order;
import model.trade.Trade;

/**
 * Observer interface for market activity notifications.
 * <p>
 * Implementations receive callbacks when trades occur
 * or order statuses change.
 */
public interface TradeObserver {
    default void onTrade(Trade trade) {}
    default void onOrderStatusChange(Order order) {}
}
