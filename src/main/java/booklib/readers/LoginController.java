package booklib.readers;

import booklib.Alerts;
import booklib.Factory;
import booklib.SceneSwitcher;
import booklib.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final AuthService authService = new AuthService(Factory.INSTANCE.getReaderDao());

    @FXML
    public void onLogin(ActionEvent e) {
        try {
            var reader = authService.login(usernameField.getText(), passwordField.getText());
            Session.setCurrentReader(reader);
            SceneSwitcher.switchTo("/booklib/MainView.fxml", (Node) e.getSource());

        } catch (Exception ex) {
            Alerts.error("Login failed", ex.getMessage());
        }
    }

    @FXML
    public void onRegister(ActionEvent e) {
        try {
            var url = LoginController.class.getResource("/booklib/RegistrationView.fxml");
            if (url == null) throw new IllegalStateException("FXML not found: /booklib/RegistrationView.fxml");

            var srcNode = (javafx.scene.Node) e.getSource();
            var stage = (javafx.stage.Stage) srcNode.getScene().getWindow();

            var loader = new javafx.fxml.FXMLLoader(url);
            var root = loader.load();

            var regController = (booklib.readers.RegistrationController) loader.getController();
            regController.setStage(stage);

            var scene = new javafx.scene.Scene((Parent) root);

            scene.getStylesheets().setAll(srcNode.getScene().getStylesheets());

            stage.setScene(scene);
            stage.setTitle("Register");

        } catch (Exception ex) {
            ex.printStackTrace();
            booklib.Alerts.error("UI error", ex.getMessage());
        }
    }
}
