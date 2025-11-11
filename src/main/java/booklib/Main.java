package booklib;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Parent rootPane = loader.load();

        var scene = new Scene(rootPane);
        stage.setTitle("BookLib");
        stage.setScene(scene);
        stage.show();
    }

    // WARNING: This main is used for running the app from terminal using `mvn javafx:run`.
    // This main will not work when running from IntelliJ IDEA, hence use IDELauncher.java instead.
    // Explanation: The issue is the Java 9+ module system that JavaFX requires.
    // However, modules are a nightmare to get working with maven, hence we do not use them at this moment.
    // As a dirty but effective hack we can use another main method that calls this one.
    // Hence, you should rather run IDELauncher.java
    public static void main(String[] args) {
        launch(args);
    }
}