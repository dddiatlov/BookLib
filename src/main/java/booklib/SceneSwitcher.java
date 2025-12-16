package booklib;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class SceneSwitcher {

    public static void switchTo(String fxmlFile, Node anyNodeFromScene) {
        try {
            var url = Objects.requireNonNull(
                    SceneSwitcher.class.getResource("/booklib/" + fxmlFile),
                    "FXML not found: /booklib/" + fxmlFile
            );

            var root = FXMLLoader.load(url);
            Stage stage = (Stage) anyNodeFromScene.getScene().getWindow();
            stage.setScene(new Scene((Parent) root));
        } catch (Exception e) {
            Alerts.error("UI error", "Cannot open screen: " + fxmlFile);
        }
    }
}
