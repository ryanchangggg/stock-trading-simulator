package repository;

import model.portfolio.Portfolio;

/**
 * Repository interface for persisting and retrieving user portfolios.
 * <p>
 * Manages the collection of stock positions for each user.
 * The portfolio is treated as an aggregate: loading retrieves all
 * positions, saving replaces the full position set for a user.
 */
public interface PortfolioRepository {

    /**
     * Retrieves the full portfolio for a user, including all positions.
     * If no positions exist, returns an empty Portfolio.
     */
    Portfolio findByUserId(long userId);

    /**
     * Persists all positions in the portfolio.
     * Replaces the existing position set for the user atomically.
     */
    void save(Portfolio portfolio);

    /**
     * Removes all positions for a given user.
     */
    void deleteByUserId(long userId);
}
