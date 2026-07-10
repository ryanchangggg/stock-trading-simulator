package ui;

import service.AppContext;
import service.Session;
import ui.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import ui.pages.DashboardPage;
import ui.pages.MarketPage;
import ui.pages.PortfolioPage;
import ui.pages.HistoryPage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Main application window after login.
 * <p>
 * Contains a dark sidebar with navigation buttons and a content
 * area that switches between pages (Dashboard, Market, Portfolio,
 * History). The UI layer calls service methods exclusively.
 */
public class MainView {

    private final AppContext context;
    private final Session session;
    private final Runnable onLogout;
    private final StackPane contentArea = new StackPane();
    private final Map<String, StackPane> pages = new LinkedHashMap<>();
    private String activePage = "";

    public MainView(AppContext context, Session session, Runnable onLogout) {
        this.context = context;
        this.session = session;
        this.onLogout = onLogout;

        pages.put("Dashboard", buildDashboard());
        pages.put("Market", buildMarket());
        pages.put("Portfolio", buildPortfolio());
        pages.put("History", buildHistory());

        showPage("Dashboard");
    }

    public Scene createScene() {
        VBox sidebar = buildSidebar();
        contentArea.getStyleClass().add("content-area");

        HBox root = new HBox(sidebar, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        return new Scene(root, 1280, 800);
    }

    // ── Sidebar ─────────────────────────────────────────

    private VBox buildSidebar() {
        Button dashboardBtn = navBtn("📊  Dashboard", "Dashboard");
        Button marketBtn    = navBtn("🏛  Market", "Market");
        Button portfolioBtn = navBtn("💼  Portfolio", "Portfolio");
        Button historyBtn   = navBtn("📜  History", "History");
        Button logoutBtn    = new Button("🚪  Sign Out");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setOnAction(e -> onLogout.run());

        VBox sidebar = new VBox(
            new javafx.scene.control.Label("StockTrader") {{
                getStyleClass().add("sidebar-header");
            }},
            new javafx.scene.control.Label("Welcome, " + session.getUsername()) {{
                getStyleClass().add("sidebar-user");
            }},
            new javafx.scene.control.Separator() {{
                setStyle("-fx-background: #2a2a4e;");
            }},
            dashboardBtn, marketBtn, portfolioBtn, historyBtn,
            new javafx.scene.layout.VBox() {{ VBox.setVgrow(this, Priority.ALWAYS); }},
            logoutBtn
        );
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private Button navBtn(String text, String page) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setOnAction(e -> showPage(page));
        return btn;
    }

    // ── Page routing ────────────────────────────────────

    private void showPage(String name) {
        activePage = name;
        StackPane page = pages.get(name);
        if (page != null) {
            contentArea.getChildren().setAll(page);
        }
    }

    // ── Pages ───────────────────────────────────────────

    private StackPane buildDashboard() {
        return new DashboardPage(context, session).build();
    }

    private StackPane buildMarket() {
        return new MarketPage(context, session).build();
    }

    private StackPane buildPortfolio() {
        return new PortfolioPage(context, session).build();
    }

    private StackPane buildHistory() {
        return new HistoryPage(context, session).build();
    }
}
