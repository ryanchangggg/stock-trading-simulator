package ui;

import service.AuthenticationService;
import service.Result;
import service.Session;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

/**
 * Login / registration screen.
 * <p>
 * Toggles between a login form and a registration form.
 * Communicates only with {@link AuthenticationService}.
 */
public class LoginView {

    private final AuthenticationService auth;
    private final Consumer<Session> onSuccess;
    private final Consumer<String> onError;
    private final Label errorLabel = new Label();
    private boolean isRegisterMode = false;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button actionButton;
    private Hyperlink toggleLink;

    public LoginView(AuthenticationService auth, Consumer<Session> onSuccess,
                     Consumer<String> onError) {
        this.auth = auth;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    public Scene createScene() {
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("form-field");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("form-field");

        errorLabel.getStyleClass().add("login-error");

        actionButton = new Button("Sign In");
        actionButton.getStyleClass().add("login-btn");
        actionButton.setOnAction(e -> handleAuth());

        toggleLink = new Hyperlink("Don't have an account? Register");
        toggleLink.setOnAction(e -> toggleMode());

        VBox form = new VBox(12,
            new Label("Stock Trading Simulator") {{
                getStyleClass().add("login-title");
            }},
            new Label("Practice trading with virtual currency") {{
                getStyleClass().add("login-subtitle");
            }},
            new Separator(),
            usernameField,
            passwordField,
            errorLabel,
            actionButton,
            toggleLink
        );
        form.getStyleClass().add("login-card");
        form.setMaxWidth(380);
        form.setAlignment(Pos.TOP_CENTER);

        StackPane root = new StackPane(form);
        root.getStyleClass().add("login-bg");
        StackPane.setAlignment(form, Pos.CENTER);

        Scene scene = new Scene(root, 1024, 700);
        scene.getStylesheets().add(
            getClass().getResource("/styles/app.css").toExternalForm());
        return scene;
    }

    private void handleAuth() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        Result<Session> result = isRegisterMode
            ? auth.register(user, pass)
            : auth.login(user, pass);
        if (result.isSuccess()) {
            onSuccess.accept(result.getValue());
        } else {
            errorLabel.setText(result.getErrorMessage());
            onError.accept(result.getErrorMessage());
        }
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        actionButton.setText(isRegisterMode ? "Create Account" : "Sign In");
        toggleLink.setText(isRegisterMode
            ? "Already have an account? Sign In"
            : "Don't have an account? Register");
        errorLabel.setText("");
    }
}
