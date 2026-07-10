package repository;

import model.order.Order;
import model.order.OrderStatus;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for persisting and retrieving Order entities.
 * <p>
 * Enables the engine to persist orders when placed and update their
 * status when filled or cancelled, without coupling to a specific
 * storage implementation.
 */
public interface OrderRepository {

    /**
     * Persists a new order or updates an existing one.
     */
    Order save(Order order);

    /**
     * Finds an order by its unique identifier.
     */
    Optional<Order> findById(long id);

    /**
     * Returns all orders for a given user.
     */
    List<Order> findByUserId(long userId);

    /**
     * Returns all orders with the given status (e.g., all PENDING orders).
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Updates only the status and fill timestamp of an order.
     *
     * @return true if the update was successful
     */
    boolean updateStatus(long orderId, OrderStatus newStatus);
}
