package booklib;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public final class SceneSwitcher {

    private SceneSwitcher() {}

    public static void switchTo(String fxmlResourcePath, Node anyNodeFromCurrentScene) {
        try {
            var url = SceneSwitcher.class.getResource(fxmlResourcePath);
            if (url == null) {
                throw new IllegalArgumentException("FXML not found on classpath: " + fxmlResourcePath);
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) anyNodeFromCurrentScene.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("UI error", "Cannot open screen: " + fxmlResourcePath + "\n" + e.getMessage());
        }
    }
}
