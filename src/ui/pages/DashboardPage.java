package ui.pages;

import model.portfolio.Portfolio.PortfolioSummary;
import model.stock.Stock;
import service.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard page showing portfolio summary cards and a chart placeholder.
 */
public class DashboardPage {

    private final PortfolioService portfolioService;
    private final MarketDataService marketService;
    private final Session session;

    public DashboardPage(AppContext ctx, Session session) {
        this.portfolioService = ctx.getPortfolioService();
        this.marketService = ctx.getMarketDataService();
        this.session = session;
    }

    public StackPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(0, 0, 0, 0));

        // Header
        VBox header = new VBox(4);
        header.getChildren().addAll(
            new Label("Dashboard") {{ getStyleClass().add("page-title"); }},
            new Label("Portfolio overview and market summary") {{ getStyleClass().add("page-subtitle"); }}
        );

        // Stat cards
        HBox cards = new HBox(16);
        cards.getChildren().addAll(
            statCard("Cash Balance", "$" + getCash(), ""),
            statCard("Portfolio Value", "$" + getPortfolioValue(), ""),
            statCard("P&L (Unrealised)", formatPnl(getPnl()), getPnlClass()),
            statCard("Available Stocks", String.valueOf(getStockCount()), "")
        );

        // Chart placeholder
        StackPane chartBox = new StackPane();
        chartBox.getStyleClass().add("chart-placeholder");
        chartBox.setMinHeight(280);
        chartBox.getChildren().add(
            new Label("📈 Performance Chart — Coming Soon") {{
                getStyleClass().add("placeholder-text");
            }}
        );

        content.getChildren().addAll(header, cards, chartBox);
        VBox.setVgrow(chartBox, Priority.ALWAYS);
        return new StackPane(content);
    }

    private StackPane statCard(String label, String value, String extraClass) {
        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-value");
        if (!extraClass.isEmpty()) valLabel.getStyleClass().add(extraClass);

        VBox card = new VBox(4,
            new Label(label) {{ getStyleClass().add("stat-label"); }},
            valLabel
        );
        card.getStyleClass().add("stat-card");
        return new StackPane(card);
    }

    private String getCash() {
        return portfolioService.getCashBalance(session)
            .map(v -> v.setScale(2, BigDecimal.ROUND_HALF_UP).toString())
            .orElse("0.00");
    }

    private String getPortfolioValue() {
        return portfolioService.getSummary(session)
            .map(s -> s.getTotalValue().setScale(2, BigDecimal.ROUND_HALF_UP).toString())
            .orElse("0.00");
    }

    private BigDecimal getPnl() {
        return portfolioService.getSummary(session)
            .map(PortfolioSummary::getUnrealisedPnl)
            .orElse(BigDecimal.ZERO);
    }

    private String formatPnl(BigDecimal pnl) {
        String prefix = pnl.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return prefix + "$" + pnl.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String getPnlClass() {
        return getPnl().compareTo(BigDecimal.ZERO) >= 0 ? "stat-positive" : "stat-negative";
    }

    private int getStockCount() {
        return marketService.getAvailableStocks().map(List::size).orElse(0);
    }
}
