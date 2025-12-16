package booklib.readingSessions;

import booklib.Factory;
import booklib.books.Book;
import booklib.readers.Reader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReadingSessionController {

    private final ReadingSessionDao sessionDao = Factory.INSTANCE.getReadingSessionDao();

    private Book book;
    private ReadingSession editingSession = null; // if not null => edit mode

    @FXML private Label bookLabel;
    @FXML private TextField pagesReadField;
    @FXML private TextField durationField;
    @FXML private DatePicker datePicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    public void setBook(Book book) {
        this.book = book;
        if (bookLabel != null) {
            bookLabel.setText(book != null ? book.getTitle() : "Book");
        }
    }

    public void setEditingSession(ReadingSession session) {
        this.editingSession = session;

        if (session != null) {
            this.book = session.getBook();
            if (bookLabel != null && book != null) bookLabel.setText(book.getTitle());

            pagesReadField.setText(String.valueOf(session.getPagesRead()));
            durationField.setText(String.valueOf(session.getDurationMinutes()));

            if (session.getCreatedAt() != null) {
                datePicker.setValue(session.getCreatedAt().toLocalDate());
            } else {
                datePicker.setValue(LocalDate.now());
            }

            if (saveButton != null) saveButton.setText("Update");
        }
    }

    @FXML
    private void initialize() {
        if (datePicker != null && datePicker.getValue() == null) {
            datePicker.setValue(LocalDate.now());
        }
    }

    @FXML
    private void onSave() {
        if (book == null) {
            showError("Book is not set.");
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

        // dev reader
        Reader r = new Reader();
        r.setId(1L);

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
                editingSession.setBook(book);
                editingSession.setReader(r);
                editingSession.setPagesRead(pages);
                editingSession.setDurationMinutes(minutes);
                editingSession.setCreatedAt(createdAt);
                sessionDao.update(editingSession);
            }

            close();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to save reading session:\n" + ex.getMessage());
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
