package booklib;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Controller {

    @FXML
    private VBox booksContainer;
    @FXML
    private Button addBookButton;
    @FXML
    private Label myBooksLabel;
    @FXML
    private Button profileButton;
    @FXML
    private Button goalButton;

    @FXML
    private void initialize() {
        // Fail fast if FXML is broken
        if (booksContainer == null) {
            throw new IllegalStateException("booksContainer was not injected. Check fx:id in MainView.fxml.");
        }

        // Temporary placeholder content
        myBooksLabel.setText("My Books");

        // Example placeholder (can be removed later)
        Label placeholder = new Label("No books yet.");
        placeholder.setStyle("-fx-text-fill: #777; -fx-font-size: 14px;");
        booksContainer.getChildren().add(placeholder);
    }

    @FXML
    private void onAddBook() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Book");
        alert.setHeaderText(null);
        alert.setContentText("Add Book functionality is not implemented yet.");
        alert.showAndWait();
    }
}
