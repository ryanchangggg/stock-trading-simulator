package ui.pages;

import model.trade.Trade;
import service.AppContext;
import service.Session;
import service.TradingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.math.RoundingMode;

/**
 * Order history page showing all completed trades.
 */
public class HistoryPage {

    private final TradingService tradingService;
    private final Session session;

    public HistoryPage(AppContext ctx, Session session) {
        this.tradingService = ctx.getTradingService();
        this.session = session;
    }

    public StackPane build() {
        VBox content = new VBox(16);

        content.getChildren().addAll(
            new Label("Trade History") {{ getStyleClass().add("page-title"); }},
            new Label("All completed transactions") {{ getStyleClass().add("page-subtitle"); }}
        );

        TableView<Trade> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(
            tc("Date", t -> t.getTradeDate().toLocalDate().toString()),
            tc("Symbol", t -> t.getSymbol()),
            tc("Type", Trade::getTradeTypeName),
            tc("Shares", t -> String.valueOf(t.getQuantity())),
            tc("Price", t -> "$" + t.getPrice().setScale(2, RoundingMode.HALF_UP)),
            tc("Total", t -> "$" + t.getTotalValue().setScale(2, RoundingMode.HALF_UP))
        );

        var trades = tradingService.getTradeHistory(session).getValue();
        if (trades != null) {
            table.setItems(FXCollections.observableArrayList(trades));
        }

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().add(table);
        return new StackPane(content);
    }

    private TableColumn<Trade, String> tc(String name,
            java.util.function.Function<Trade, String> fn) {
        return new TableColumn<>(name) {{
            setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        }};
    }
}
