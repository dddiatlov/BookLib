package booklib.readers;

import booklib.Alerts;
import booklib.Factory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;

public class RegistrationController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @Setter
    private javafx.stage.Stage stage;
    private final AuthService authService = new AuthService(Factory.INSTANCE.getReaderDao());

    @FXML
    public void onRegister(javafx.event.ActionEvent e) {
        try {
            String username = usernameField.getText();
            String pass = passwordField.getText();
            String confirm = confirmPasswordField.getText();

            if (pass == null || confirm == null || !pass.equals(confirm)) {
                throw new IllegalArgumentException("Passwords do not match.");
            }

            authService.register(username, pass);

            booklib.Alerts.info("Success", "User registered. Now you can login.");
            goBackToLogin();

        } catch (Exception ex) {
            booklib.Alerts.error("Register failed", ex.getMessage());
        }
    }

    @FXML
    public void onCancel(javafx.event.ActionEvent e) {
        goBackToLogin();
    }

    private void goBackToLogin() {
        try {
            var url = RegistrationController.class.getResource("/booklib/LoginView.fxml");
            if (url == null) throw new IllegalStateException("FXML not found: /booklib/LoginView.fxml");

            var loader = new javafx.fxml.FXMLLoader(url);
            var root = loader.load();
            var newScene = new javafx.scene.Scene((Parent) root);

            if (stage != null && stage.getScene() != null) {
                newScene.getStylesheets().setAll(stage.getScene().getStylesheets());
            }

            stage.setScene(newScene);
            stage.setTitle("Login");

        } catch (Exception ex) {
            ex.printStackTrace();
            booklib.Alerts.error("UI error", ex.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
