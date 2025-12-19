package booklib.books;

import booklib.Alerts;
import booklib.Factory;
import booklib.Session;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Getter;

import java.util.List;

public class AddBookController {

    private static final String STATUS = "WANT_TO_READ";

    @FXML private ComboBox<Book> bookCombo;
    @FXML private Label authorValue;
    @FXML private Label pagesValue;
    @FXML private Label genreValue;
    @FXML private Label languageValue;

    private final BookDao bookDao = Factory.INSTANCE.getBookDao();

    @Getter
    private boolean saved = false;

    @FXML
    private void initialize() {
        List<Book> all = bookDao.findAll();
        bookCombo.getItems().setAll(all);

        bookCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Book b) {
                if (b == null) return "";
                String a = (b.getAuthor() == null || b.getAuthor().isBlank()) ? "" : (" — " + b.getAuthor());
                return b.getTitle() + a;
            }
            @Override public Book fromString(String string) { return null; }
        });

        bookCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    String a = (item.getAuthor() == null || item.getAuthor().isBlank()) ? "" : (" — " + item.getAuthor());
                    setText(item.getTitle() + a);
                }
            }
        });

        bookCombo.valueProperty().addListener((obs, oldVal, newVal) -> showDetails(newVal));

        if (!all.isEmpty()) {
            bookCombo.getSelectionModel().select(0);
            showDetails(all.get(0));
        } else {
            showDetails(null);
            Alerts.info("No books", "Database has no books yet. Use 'Import CSV' to load /booklib/books.csv.");
        }
    }

    private void showDetails(Book b) {
        authorValue.setText(b == null ? "—" : (b.getAuthor() == null || b.getAuthor().isBlank() ? "—" : b.getAuthor()));
        pagesValue.setText(b == null ? "—" : String.valueOf(b.getPages()));
        genreValue.setText(b == null ? "—" : (b.getGenre() == null || b.getGenre().isBlank() ? "—" : b.getGenre()));
        languageValue.setText(b == null ? "—" : (b.getLanguage() == null || b.getLanguage().isBlank() ? "—" : b.getLanguage()));
    }

    @FXML
    public void onSave() {
        try {
            Long readerId = Long.valueOf(Session.requireReaderId());

            Book selected = bookCombo.getValue();
            if (selected == null || selected.getId() == null) {
                Alerts.error("Validation", "Please select a book from the list.");
                return;
            }

            List<Book> myBooks = bookDao.findByReaderId(readerId);
            boolean alreadyAdded = myBooks.stream()
                    .anyMatch(b -> b.getId() != null && b.getId().equals(selected.getId()));

            if (alreadyAdded) {
                Alerts.info("Already added", "This book is already in 'My Books'.");
                saved = false;
                bookCombo.requestFocus();
                return;
            }
            bookDao.addBookForReader(selected.getId(), readerId, STATUS);
            saved = true;
            close();

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("Error", e.getMessage());
        }
    }

    @FXML
    public void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) bookCombo.getScene().getWindow();
        stage.close();
    }
}
