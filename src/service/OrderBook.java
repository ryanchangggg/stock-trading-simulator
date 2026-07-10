package service;

import model.order.Order;
import model.order.OrderStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory registry of all pending orders in the trading system.
 * <p>
 * The OrderBook maintains a central collection of orders that have
 * been accepted but not yet filled (limit orders, stop-loss orders).
 * On each simulation tick, the engine queries the order book for
 * orders whose price conditions have been met by the current market.
 * <p>
 * Orders are indexed by both ID and symbol for efficient lookups.
 * Thread safety is not required as the simulation is single-threaded.
 */
public class OrderBook {

    private final Map<Long, Order> ordersById;
    private final Map<String, List<Order>> ordersBySymbol;

    /**
     * Constructs an empty OrderBook.
     */
    public OrderBook() {
        this.ordersById = new HashMap<>();
        this.ordersBySymbol = new HashMap<>();
    }

    /**
     * Adds an order to the book. The order must be in PENDING status.
     *
     * @param order the order to add
     * @throws IllegalArgumentException if the order is not PENDING
     */
    public void add(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                "Only PENDING orders can be added to the order book");
        }
        ordersById.put(order.getId(), order);
        ordersBySymbol.computeIfAbsent(order.getSymbol().toUpperCase(),
            k -> new ArrayList<>()).add(order);
    }

    /**
     * Removes an order from the book by its ID.
     *
     * @param orderId the order identifier
     */
    public void remove(long orderId) {
        Order removed = ordersById.remove(orderId);
        if (removed != null) {
            String symbol = removed.getSymbol().toUpperCase();
            List<Order> symbolOrders = ordersBySymbol.get(symbol);
            if (symbolOrders != null) {
                symbolOrders.remove(removed);
                if (symbolOrders.isEmpty()) {
                    ordersBySymbol.remove(symbol);
                }
            }
        }
    }

    /**
     * Finds an order by its unique identifier.
     */
    public Optional<Order> findById(long orderId) {
        return Optional.ofNullable(ordersById.get(orderId));
    }

    /**
     * Returns all pending orders in the book.
     */
    public List<Order> getAllPending() {
        return Collections.unmodifiableList(
            new ArrayList<>(ordersById.values()));
    }

    /**
     * Returns all pending orders for a specific stock symbol.
     */
    public List<Order> getPendingBySymbol(String symbol) {
        List<Order> symbolOrders = ordersBySymbol.get(symbol.toUpperCase());
        if (symbolOrders == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(symbolOrders));
    }

    /**
     * Returns the number of pending orders in the book.
     */
    public int size() {
        return ordersById.size();
    }

    /**
     * Returns true if the order book contains no pending orders.
     */
    public boolean isEmpty() {
        return ordersById.isEmpty();
    }

    /**
     * Removes all orders from the book.
     */
    public void clear() {
        ordersById.clear();
        ordersBySymbol.clear();
    }

    /**
     * Replaces the order in the book with an updated copy.
     * Used when an order's status changes (e.g., from PENDING to FILLED).
     *
     * @param order the updated order
     */
    public void update(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            remove(order.getId());
        } else {
            ordersById.put(order.getId(), order);
        }
    }
}
