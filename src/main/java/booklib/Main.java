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

        var root = FXMLLoader.load(url);
        stage.setTitle("BookLib - Login");
        stage.setScene(new Scene((Parent) root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
