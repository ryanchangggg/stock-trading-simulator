package main;

import ui.LoginView;
import ui.MainView;

import service.AppContext;
import service.Session;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the Stock Trading Simulator desktop application.
 * <p>
 * Initialises the application context, loads market data, shows the
 * login screen, and manages the main application lifecycle.
 * The UI communicates exclusively through service-layer objects.
 */
public class StockTradingApp extends Application {

    private static AppContext context;
    private Stage primaryStage;

    @Override
    public void init() {
        context = new AppContext();
        context.loadMarketData("data");
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Stock Trading Simulator");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        showLogin();
        primaryStage.show();
    }

    private void showLogin() {
        LoginView login = new LoginView(context.getAuthService(),
            this::onLoginSuccess, this::onLoginError);
        Scene scene = login.createScene();
        primaryStage.setScene(scene);
    }

    private void onLoginSuccess(Session session) {
        MainView main = new MainView(context, session, this::showLogin);
        Scene scene = main.createScene();
        scene.getStylesheets().add(
            getClass().getResource("/styles/app.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    private void onLoginError(String error) {
        // handled inside LoginView
    }

    public static void main(String[] args) {
        launch(args);
    }
}
