package booklib;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import booklib.readingSessions.ReadingSession;
import booklib.readingSessions.ReadingSessionController;
import booklib.readingSessions.ReadingSessionDao;

import java.io.IOException;

public class Controller {

    private final ReadingSessionDao sessionDao = Factory.INSTANCE.getReadingSessionDao();

    @FXML
    private ListView<ReadingSession> sessionsListView;

    @FXML
    private Button addSessionButton;

    @FXML
    private Button deleteSessionButton;

    @FXML
    void initialize() {
        sessionsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ReadingSession item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String readerName = item.getReader() != null ? item.getReader().getName() : "?";
                    String bookTitle = item.getBook() != null ? item.getBook().getTitle() : "?";
                    String date = item.getCreatedAt() != null ? item.getCreatedAt().toLocalDate().toString() : "";
                    setText(date + " | " + readerName + " – " + bookTitle +
                            " (" + item.getPagesRead() + " pages, " + item.getDurationMinutes() + " min)");
                }
            }
        });

        refreshList();
    }

    private void refreshList() {
        sessionsListView.setItems(FXCollections.observableArrayList(sessionDao.findAllSortedByDate()));
    }

    private void openSessionWindow(ReadingSession sessionToEdit) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ReadingSessionView.fxml"));
        Parent pane;
        try {
            pane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Error loading ReadingSessionView.fxml", e);
        }

        var controller = (ReadingSessionController) loader.getController();
        if (sessionToEdit != null) {
            controller.setEditMode(sessionToEdit);
        }

        var scene = new Scene(pane);
        var stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(sessionToEdit == null ? "New reading session" : "Edit reading session");
        stage.setScene(scene);
        stage.showAndWait();

        refreshList();
    }

    @FXML
    void addSessionButtonAction(ActionEvent event) {
        openSessionWindow(null);
    }

    @FXML
    void sessionsListViewMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            var selected = sessionsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openSessionWindow(selected);
            }
        }
    }

    @FXML
    void deleteSessionButtonAction(ActionEvent event) {
        var selected = sessionsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        var alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you really want to delete the selected reading session?");
        var result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        sessionDao.delete(selected.getId());
        refreshList();
    }
}
