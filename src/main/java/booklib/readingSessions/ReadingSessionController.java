package booklib.readingSessions;

import booklib.Factory;
import booklib.Session;
import booklib.books.Book;
import booklib.readers.Reader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReadingSessionController {

    private final ReadingSessionDao sessionDao = Factory.INSTANCE.getReadingSessionDao();

    @FXML private Label headerLabel;
    @FXML private DatePicker datePicker;
    @FXML private TextField pagesReadField;
    @FXML private TextField durationField;
    @FXML private Button saveButton;

    private Book book;
    private ReadingSession editingSession;

    public void setBook(Book book) {
        this.book = book;
        if (headerLabel != null && book != null) {
            headerLabel.setText("Reading session for: " + book.getTitle());
        }
    }

    public void setEditingSession(ReadingSession session) {
        this.editingSession = session;
        if (session != null) {
            if (datePicker != null && session.getCreatedAt() != null) {
                datePicker.setValue(session.getCreatedAt().toLocalDate());
            }
            if (pagesReadField != null) pagesReadField.setText(String.valueOf(session.getPagesRead()));
            if (durationField != null) durationField.setText(String.valueOf(session.getDurationMinutes()));
        }
    }

    @FXML
    private void initialize() {
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }
    }

    @FXML
    private void onSave() {
        if (book == null) {
            showError("Book is not set.");
            return;
        }

        Reader r = Session.getCurrentReader();
        if (r == null || r.getId() == null) {
            showError("No logged-in reader. Please login again.");
            return;
        }

        int pages;
        int minutes;

        try {
            pages = Integer.parseInt(pagesReadField.getText().trim());
            minutes = Integer.parseInt(durationField.getText().trim());
        } catch (Exception e) {
            showError("Pages read and duration must be whole numbers.");
            return;
        }

        if (pages <= 0) { showError("Pages read must be greater than 0."); return; }
        if (minutes <= 0) { showError("Duration must be greater than 0 minutes."); return; }

        LocalDateTime createdAt;
        LocalDate d = datePicker.getValue();
        if (d != null) createdAt = d.atTime(LocalDateTime.now().toLocalTime());
        else createdAt = LocalDateTime.now();

        try {
            if (editingSession == null) {
                ReadingSession session = new ReadingSession();
                session.setBook(book);
                session.setReader(r);
                session.setPagesRead(pages);
                session.setDurationMinutes(minutes);
                session.setCreatedAt(createdAt);

                sessionDao.create(session);
            } else {
                editingSession.setPagesRead(pages);
                editingSession.setDurationMinutes(minutes);
                editingSession.setCreatedAt(createdAt);
                // reader/book уже есть, но оставим корректно:
                editingSession.setReader(r);
                editingSession.setBook(book);

                sessionDao.update(editingSession);
            }
            close();
        } catch (Exception ex) {
            showError("Cannot save session: " + ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
