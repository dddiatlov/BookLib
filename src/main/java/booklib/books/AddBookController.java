package booklib.books;

import booklib.Alerts;
import booklib.Factory;
import booklib.Session;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddBookController {

    private static final String STATUS_WANT = "WANT_TO_READ";

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField pagesField;
    @FXML private TextField genreField;
    @FXML private ComboBox<String> languageCombo;

    private final BookDao bookDao = Factory.INSTANCE.getBookDao();

    // чтобы главный контроллер мог понять: добавили или нет
    private boolean saved = false;

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void initialize() {
        languageCombo.getItems().setAll("en", "sk", "ru", "uk", "de", "cs", "pl");
        languageCombo.getSelectionModel().select("en");
    }

    @FXML
    public void onSave() {
        try {
            Long readerId = Long.valueOf(Session.requireReaderId());

            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String genre = genreField.getText().trim();
            String lang = (languageCombo.getValue() == null) ? "en" : languageCombo.getValue();

            int pages;
            try {
                pages = Integer.parseInt(pagesField.getText().trim());
            } catch (Exception ex) {
                Alerts.error("Validation", "Pages must be a number.");
                return;
            }

            if (title.isBlank()) { Alerts.error("Validation", "Title cannot be empty."); return; }
            if (genre.isBlank()) { Alerts.error("Validation", "Genre cannot be empty."); return; }
            if (pages <= 0) { Alerts.error("Validation", "Pages must be > 0."); return; }

            Book b = new Book();
            b.setTitle(title);
            b.setAuthor(author.isBlank() ? null : author);
            b.setGenre(genre);
            b.setPages(pages);
            b.setLanguage(lang);

            // 1) INSERT into book
            bookDao.add(b);

            if (b.getId() == null) {
                throw new IllegalStateException("Book ID is null after bookDao.add(b). Check MysqlBookDao.add(Book) to set generated ID.");
            }

            // 2) link in book_status
            bookDao.addBookForReader(b.getId(), readerId, STATUS_WANT);

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
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
