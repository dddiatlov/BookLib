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

    private final ReadingSessionDao readingSessionDao = Factory.INSTANCE.getReadingSessionDao();

    @FXML private Label headerLabel;
    @FXML private Label limitLabel;

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
        applyInputConstraints();
    }

    public void setEditingSession(ReadingSession session) {
        this.editingSession = session;

        if (session != null) {
            if (datePicker != null && session.getCreatedAt() != null) datePicker.setValue(session.getCreatedAt().toLocalDate());
            if (pagesReadField != null) pagesReadField.setText(String.valueOf(session.getPagesRead()));
            if (durationField != null) durationField.setText(String.valueOf(session.getDurationMinutes()));
        }
        applyInputConstraints();
    }

    @FXML
    private void initialize() {
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }
        applyInputConstraints();
    }

    private void applyInputConstraints() {
        if (durationField != null) {
            durationField.setTextFormatter(digitsOnlyFormatter());
        }

        if (pagesReadField != null) {
            if (book == null) {
                pagesReadField.setTextFormatter(digitsOnlyFormatter());
                if (limitLabel != null) limitLabel.setText("");
                return;
            }

            int remaining = remainingPagesForThisBook();

            if (remaining <= 0) {
                pagesReadField.setText("0");
                pagesReadField.setDisable(true);
                saveButton.setDisable(true);
                if (limitLabel != null) {
                    limitLabel.setText("No pages remaining. Book is already finished.");
                }
                return;
            }

            pagesReadField.setDisable(false);
            saveButton.setDisable(false);

            pagesReadField.setTextFormatter(rangedIntFormatter(1, remaining));
            if (limitLabel != null) {
                limitLabel.setText("You can add at most: " + remaining + " pages in this session.");
            }
            clampFieldToRange(pagesReadField, 1, remaining);
        }
    }

    private int remainingPagesForThisBook() {
        long readerId = Session.requireReaderId();
        int bookPages = Math.max(0, book.getPages());

        int alreadyRead = readingSessionDao.sumPagesForReaderAndBook(readerId, book.getId());

        if (editingSession != null && editingSession.getId() != null) {
            alreadyRead -= editingSession.getPagesRead();
            if (alreadyRead < 0) alreadyRead = 0;
        }
        return Math.max(0, bookPages - alreadyRead);
    }

    private TextFormatter<String> digitsOnlyFormatter() {
        return new TextFormatter<>(change -> {
            String t = change.getControlNewText();
            if (t.isBlank()) return change;
            return t.matches("\\d{0,9}") ? change : null;
        });
    }

    private TextFormatter<String> rangedIntFormatter(int min, int max) {
        return new TextFormatter<>(change -> {
            String t = change.getControlNewText();
            if (t.isBlank()) return change;
            if (!t.matches("\\d{0,9}")) return null;

            try {
                int v = Integer.parseInt(t);
                if (v < min || v > max) return null;
            } catch (NumberFormatException ex) {
                return null;
            }
            return change;
        });
    }

    private void clampFieldToRange(TextField field, int min, int max) {
        String t = field.getText();
        if (t == null || t.isBlank()) return;
        try {
            int v = Integer.parseInt(t.trim());
            if (v < min) v = min;
            if (v > max) v = max;
            field.setText(String.valueOf(v));
        } catch (Exception ignored) {
            field.clear();
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

        if (minutes <= 0) {
            showError("Duration must be greater than 0 minutes.");
            return;
        }

        int remaining = remainingPagesForThisBook();
        if (remaining <= 0) {
            showError("Book is already finished. You cannot add more pages.");
            return;
        }
        if (pages < 1 || pages > remaining) {
            showError("You can add at most " + remaining + " pages for this book.");
            return;
        }

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

                readingSessionDao.create(session);
            } else {
                editingSession.setPagesRead(pages);
                editingSession.setDurationMinutes(minutes);
                editingSession.setCreatedAt(createdAt);
                editingSession.setReader(r);
                editingSession.setBook(book);

                readingSessionDao.update(editingSession);
            }
            close();
        } catch (Exception ex) {
            ex.printStackTrace();
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
