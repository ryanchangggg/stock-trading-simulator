package repository;

import model.trade.Trade;
import java.util.List;

/**
 * Repository interface for persisting and retrieving completed trades.
 * <p>
 * Provides an audit trail abstraction so the engine can record
 * completed trades without depending on a concrete data store.
 */
public interface TradeRepository {

    /**
     * Persists a completed trade and returns it with any generated state
     * (e.g., an auto-assigned database ID).
     */
    Trade save(Trade trade);

    /**
     * Returns all trades for a given user, ordered by timestamp descending.
     */
    List<Trade> findByUserId(long userId);

    /**
     * Returns all trades for a specific symbol.
     */
    List<Trade> findBySymbol(String symbol);
}
