package ui.pages;

import model.portfolio.Position;
import service.AppContext;
import service.PortfolioService;
import service.Session;
import service.TradingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Portfolio page showing current holdings with P&L.
 */
public class PortfolioPage {

    private final PortfolioService portfolioService;
    private final TradingService tradingService;
    private final Session session;
    private final AppContext context;

    public PortfolioPage(AppContext ctx, Session session) {
        this.portfolioService = ctx.getPortfolioService();
        this.tradingService = ctx.getTradingService();
        this.session = session;
        this.context = ctx;
    }

    public StackPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(0, 0, 0, 0));

        content.getChildren().addAll(
            new Label("Portfolio") {{ getStyleClass().add("page-title"); }},
            new Label("Your current holdings and performance") {{ getStyleClass().add("page-subtitle"); }}
        );

        // Summary
        var summary = portfolioService.getSummary(session).getValue();
        HBox cards = new HBox(16);
        cards.getChildren().addAll(
            statCard("Total Value", "$" + (summary != null ? summary.getTotalValue().setScale(2, RoundingMode.HALF_UP) : "0.00"), ""),
            statCard("Unrealised P&L", 
                (summary != null && summary.getUnrealisedPnl().compareTo(BigDecimal.ZERO) >= 0 ? "+" : "")
                + "$" + (summary != null ? summary.getUnrealisedPnl().setScale(2, RoundingMode.HALF_UP) : "0.00"),
                summary != null && summary.getUnrealisedPnl().compareTo(BigDecimal.ZERO) >= 0 ? "stat-positive" : "stat-negative")
        );

        // Positions table
        TableView<Position> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
            col("Symbol", p -> p.getSymbol()),
            col("Shares", p -> String.valueOf(p.getQuantity())),
            col("Avg Cost", p -> "$" + p.getAverageCost().setScale(2, RoundingMode.HALF_UP)),
            col("Market Value", p -> "$" + p.getMarketValue(getPrice(p.getSymbol())).setScale(2, RoundingMode.HALF_UP)),
            col("P&L", p -> formatPnl(p.getUnrealisedPnl(getPrice(p.getSymbol()))),
                p -> pnlClass(p.getUnrealisedPnl(getPrice(p.getSymbol()))))
        );

        var positions = portfolioService.getPositions(session).getValue();
        if (positions != null) {
            table.setItems(FXCollections.observableArrayList(positions));
        }

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(cards, table);
        return new StackPane(content);
    }

    private StackPane statCard(String label, String value, String extraClass) {
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        if (!extraClass.isEmpty()) val.getStyleClass().add(extraClass);
        VBox card = new VBox(4, new Label(label) {{ getStyleClass().add("stat-label"); }}, val);
        card.getStyleClass().add("stat-card");
        return new StackPane(card);
    }

    private BigDecimal getPrice(String symbol) {
        return context.getMarketDataService().getPrice(symbol)
            .map(p -> p).orElse(BigDecimal.ZERO);
    }

    private String formatPnl(BigDecimal pnl) {
        return (pnl.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "")
            + "$" + pnl.setScale(2, RoundingMode.HALF_UP);
    }

    private String pnlClass(BigDecimal pnl) {
        return pnl.compareTo(BigDecimal.ZERO) >= 0 ? "stat-positive" : "stat-negative";
    }

    private TableColumn<Position, String> col(String name,
            java.util.function.Function<Position, String> val) {
        return new TableColumn<>(name) {{
            setCellValueFactory(d -> new SimpleStringProperty(val.apply(d.getValue())));
        }};
    }

    private TableColumn<Position, String> col(String name,
            java.util.function.Function<Position, String> val,
            java.util.function.Function<Position, String> style) {
        TableColumn<Position, String> tc = new TableColumn<>(name);
        tc.setCellValueFactory(d -> new SimpleStringProperty(val.apply(d.getValue())));
        tc.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(item);
                    Position p = (Position) getTableRow().getItem();
                    String cls = style.apply(p);
                    setStyle(cls.contains("positive") ? "-fx-text-fill: #27ae60;" :
                             cls.contains("negative") ? "-fx-text-fill: #e74c3c;" : "");
                }
            }
        });
        return tc;
    }
}
