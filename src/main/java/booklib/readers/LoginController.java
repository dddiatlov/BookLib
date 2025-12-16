package booklib.readers;

import booklib.Alerts;
import booklib.Factory;
import booklib.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final AuthService authService = new AuthService(Factory.INSTANCE.getReaderDao());

    @FXML
    public void onLogin(ActionEvent e) {
        try {
            authService.login(usernameField.getText(), passwordField.getText());

            // ✅ после успешного логина — на MainView.fxml
            SceneSwitcher.switchTo("MainView.fxml", (Node) e.getSource());

        } catch (Exception ex) {
            Alerts.error("Login failed", ex.getMessage());
        }
    }

    @FXML
    public void onRegister(ActionEvent e) {
        try {
            authService.register(usernameField.getText(), passwordField.getText());
            Alerts.info("Success", "User registered. Now you can login.");
        } catch (Exception ex) {
            Alerts.error("Register failed", ex.getMessage());
        }
    }
}
