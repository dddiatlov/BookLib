package booklib;

import booklib.readingSessions.ReadingSession;
import booklib.readingSessions.ReadingSessionController;
import booklib.readingSessions.ReadingSessionDao;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {

    // DAO для работы с сессиями
    private final ReadingSessionDao sessionDao = Factory.INSTANCE.getReadingSessionDao();

    // --------- элементы из MainView.fxml ---------

    @FXML
    private ListView<ReadingSession> sessionsListView;

    @FXML
    private Button addSessionButton;

    @FXML
    private Button deleteSessionButton;

    @FXML
    private Button addBookButton; // просто держим ссылку, логика по желанию

    // --------- инициализация экрана ---------

    @FXML
    private void initialize() {
        // Кастомный вывод строки в ListView
        sessionsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ReadingSession item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    String readerName = (item.getReader() != null)
                            ? item.getReader().getName()
                            : "?";

                    String bookTitle = (item.getBook() != null)
                            ? item.getBook().getTitle()
                            : "?";

                    String date = (item.getCreatedAt() != null)
                            ? item.getCreatedAt().toLocalDate().toString()
                            : "";

                    setText(date + " | " + readerName + " – " + bookTitle +
                            " (" + item.getPagesRead() + " pages, " + item.getDurationMinutes() + " min)");
                }
            }
        });

        refreshList();
    }

    // Обновляет список сессий из БД
    private void refreshList() {
        sessionsListView.setItems(
                FXCollections.observableArrayList(
                        sessionDao.findAllSortedByDate()
                )
        );
    }

    // --------- работа с окном сессии ---------

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

    // --------- @FXML-хендлеры из MainView.fxml ---------

    @FXML
    private void addSessionButtonAction(ActionEvent event) {
        openSessionWindow(null);
    }

    @FXML
    private void deleteSessionButtonAction(ActionEvent event) {
        var selected = sessionsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        var alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Do you really want to delete the selected reading session?"
        );
        var result = alert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        sessionDao.delete(selected.getId());
        refreshList();
    }

    @FXML
    private void sessionsListViewMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            var selected = sessionsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openSessionWindow(selected);
            }
        }
    }

    @FXML
    private void onAddBook(ActionEvent event) {
        // пока просто заглушка. Можешь потом открыть отдельное окно "Add Book"
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Add Book is not implemented yet.");
        alert.showAndWait();
    }
}

