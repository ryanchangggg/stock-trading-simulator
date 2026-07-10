package ui.pages;

import model.stock.Stock;
import service.AppContext;
import service.MarketDataService;
import service.Session;
import service.TradingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

/**
 * Market watch page showing all available stocks and their current prices.
 * Users can trigger a buy/sell dialog from here.
 */
public class MarketPage {

    private final MarketDataService marketService;
    private final TradingService tradingService;
    private final Session session;
    private final AppContext context;

    public MarketPage(AppContext ctx, Session session) {
        this.marketService = ctx.getMarketDataService();
        this.tradingService = ctx.getTradingService();
        this.session = session;
        this.context = ctx;
    }

    public StackPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(0, 0, 0, 0));

        content.getChildren().addAll(
            new Label("Market Watch") {{ getStyleClass().add("page-title"); }},
            new Label("Browse available stocks and current prices") {{ getStyleClass().add("page-subtitle"); }}
        );

        TableView<Stock> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Stock, String> symCol = new TableColumn<>("Symbol");
        symCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSymbol()));

        TableColumn<Stock, String> nameCol = new TableColumn<>("Company");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName()));

        TableColumn<Stock, String> sectorCol = new TableColumn<>("Sector");
        sectorCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSector()));

        TableColumn<Stock, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(d -> new SimpleStringProperty(
            "$" + d.getValue().getCurrentPrice().setScale(2, java.math.RoundingMode.HALF_UP)));

        table.getColumns().addAll(symCol, nameCol, sectorCol, priceCol);
        table.setItems(FXCollections.observableArrayList(
            marketService.getAvailableStocks().getValue()));

        // Double-click to open order dialog
        table.setRowFactory(tv -> {
            TableRow<Stock> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    new OrderDialog(context, session, row.getItem()).show();
                }
            });
            return row;
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().add(table);
        return new StackPane(content);
    }
}
