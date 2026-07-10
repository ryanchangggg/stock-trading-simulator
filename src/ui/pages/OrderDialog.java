package ui.pages;

import model.stock.Stock;
import service.AppContext;
import service.Result;
import service.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.math.BigDecimal;

/**
 * Modal dialog for placing buy/sell/limit/stop-loss orders.
 * <p>
 * Communicates only with {@link com.stocktrader.service.TradingService}.
 */
public class OrderDialog {

    private final AppContext context;
    private final Session session;
    private final Stock stock;
    private final Stage dialog = new Stage();
    private final Label feedbackLabel = new Label();
    private boolean isBuy = true;

    public OrderDialog(AppContext ctx, Session session, Stock stock) {
        this.context = ctx;
        this.session = session;
        this.stock = stock;
    }

    public void show() {
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setScene(buildScene());
        dialog.show();
    }

    private Scene buildScene() {
        String currentPrice = "$" + stock.getCurrentPrice().setScale(2, BigDecimal.ROUND_HALF_UP);

        Label title = new Label("Trade " + stock.getSymbol());
        title.getStyleClass().add("dialog-title");

        Label subtitle = new Label(stock.getCompanyName() + "  ·  " + currentPrice);
        subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Buy/Sell toggle
        ToggleGroup group = new ToggleGroup();
        RadioButton buyRadio = new RadioButton("Buy");
        buyRadio.setToggleGroup(group);
        buyRadio.setSelected(true);
        RadioButton sellRadio = new RadioButton("Sell");
        sellRadio.setToggleGroup(group);
        group.selectedToggleProperty().addListener((o, ov, nv) -> isBuy = nv == buyRadio);
        HBox toggleRow = new HBox(16, buyRadio, sellRadio);

        // Order type
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Market", "Limit", "Stop Loss");
        typeCombo.setValue("Market");

        // Quantity
        TextField qtyField = new TextField();
        qtyField.setPromptText("Number of shares");
        qtyField.getStyleClass().add("form-field");

        // Limit / Stop price (hidden for Market)
        TextField limitField = new TextField();
        limitField.setPromptText("Limit / Stop price");
        limitField.getStyleClass().add("form-field");
        limitField.setVisible(false);

        typeCombo.setOnAction(e -> limitField.setVisible(
            !"Market".equals(typeCombo.getValue())));

        feedbackLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        Button submitBtn = new Button("Place Order");
        submitBtn.getStyleClass().addAll("action-btn", "buy-btn");
        submitBtn.setOnAction(e -> {
            boolean buy = isBuy;
            String type = typeCombo.getValue();
            int qty;
            try {
                qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                feedbackLabel.setText("Enter a valid positive quantity");
                return;
            }

            BigDecimal limit = null;
            if (!"Market".equals(type)) {
                try {
                    limit = new BigDecimal(limitField.getText().trim());
                    if (limit.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    feedbackLabel.setText("Enter a valid positive price");
                    return;
                }
            }

            Result<?> result;
            var svc = context.getTradingService();
            if ("Market".equals(type)) {
                result = buy ? svc.buyMarket(session, stock.getSymbol(), qty)
                             : svc.sellMarket(session, stock.getSymbol(), qty);
            } else if ("Limit".equals(type)) {
                result = buy ? svc.buyLimit(session, stock.getSymbol(), qty, limit)
                             : svc.sellLimit(session, stock.getSymbol(), qty, limit);
            } else {
                result = buy ? svc.buyStopLoss(session, stock.getSymbol(), qty, limit)
                             : svc.sellStopLoss(session, stock.getSymbol(), qty, limit);
            }

            if (result.isSuccess()) {
                feedbackLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
                feedbackLabel.setText("Order placed successfully!");
            } else {
                feedbackLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
                feedbackLabel.setText(result.getErrorMessage());
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("action-btn", "neutral-btn");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(12, submitBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(14,
            title, subtitle,
            new Separator(),
            toggleRow,
            new Label("Order Type:"),
            typeCombo,
            new Label("Quantity:"),
            qtyField,
            new Label("Limit / Stop Price:") {{
                managedProperty().bind(limitField.visibleProperty());
            }},
            limitField,
            feedbackLabel,
            buttons
        );
        form.getStyleClass().add("dialog-card");
        form.setMaxWidth(380);

        StackPane root = new StackPane(form);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: rgba(0,0,0,0.35);");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/styles/app.css").toExternalForm());
        return scene;
    }
}
