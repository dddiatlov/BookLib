package booklib;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlUrl = Main.class.getResource("/booklib/MainView.fxml");
        System.out.println("FXML URL = " + fxmlUrl);

        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML not found: /booklib/MainView.fxml");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent rootPane = loader.load();

        Scene scene = new Scene(rootPane);
        stage.setTitle("BookLib");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
