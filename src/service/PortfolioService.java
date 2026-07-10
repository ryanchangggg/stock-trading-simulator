package service;

import model.market.Market;
import model.portfolio.Portfolio;
import model.portfolio.Portfolio.PortfolioSummary;
import model.portfolio.Position;
import model.user.User;
import repository.PortfolioRepository;
import repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;

/**
 * Provides portfolio-related queries to the UI layer.
 * <p>
 * Loads the user's portfolio and computes summary metrics
 * against the current market prices.
 */
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final Market market;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            UserRepository userRepository, Market market) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.market = market;
    }

    public Result<Portfolio> getPortfolio(Session session) {
        Portfolio portfolio = portfolioRepository.findByUserId(
            session.getUserId());
        return Result.success(portfolio);
    }

    public Result<PortfolioSummary> getSummary(Session session) {
        Portfolio portfolio = portfolioRepository.findByUserId(
            session.getUserId());
        PortfolioSummary summary = portfolio.getSummary(market::getCurrentPrice);
        return Result.success(summary);
    }

    public Result<List<Position>> getPositions(Session session) {
        Portfolio portfolio = portfolioRepository.findByUserId(
            session.getUserId());
        return Result.success(portfolio.getPositions());
    }

    public Result<BigDecimal> getCashBalance(Session session) {
        User user = userRepository.findById(session.getUserId()).orElse(null);
        if (user == null) return Result.failure("User not found");
        return Result.success(user.getCashBalance());
    }
}
