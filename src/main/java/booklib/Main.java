package booklib;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        var url = Main.class.getResource("LoginView.fxml");
        if (url == null) {
            throw new IllegalStateException("FXML not found: LoginView.fxml");
        }

        Parent root = FXMLLoader.load(url);
        Scene scene = new Scene(root);

        var cssUrl = Main.class.getResource("/styles/light-theme.css");
        if (cssUrl == null) {
            throw new IllegalStateException("CSS not found: /styles/light-theme.css (check resources path)");
        }
        scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setScene(scene);
        stage.setTitle("BookLib");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
