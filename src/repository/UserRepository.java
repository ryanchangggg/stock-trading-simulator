package repository;

import model.user.User;
import java.util.Optional;

/**
 * Repository interface for persisting and retrieving User entities.
 * <p>
 * Follows the Repository pattern to abstract the data store
 * (SQLite, in-memory, etc.) from the domain and engine layers.
 */
public interface UserRepository {

    /**
     * Finds a user by their unique identifier.
     */
    Optional<User> findById(long id);

    /**
     * Finds a user by their username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Persists the user. If the user already exists, updates their state
     * (including cash balance and portfolio changes).
     */
    User save(User user);
}
