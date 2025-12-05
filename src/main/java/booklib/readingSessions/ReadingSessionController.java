package booklib.readingSessions;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import booklib.Factory;
import booklib.books.Book;
import booklib.books.BookDao;
import booklib.readers.Reader;
import booklib.readers.ReaderDao;

public class ReadingSessionController {

    private final ReadingSessionModel model = new ReadingSessionModel();
    private final ReaderDao readerDao = Factory.INSTANCE.getReaderDao();
    private final BookDao bookDao = Factory.INSTANCE.getBookDao();
    private final ReadingSessionDao readingSessionDao = Factory.INSTANCE.getReadingSessionDao();

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<Reader> readerComboBox;

    @FXML
    private ComboBox<Book> bookComboBox;

    @FXML
    private TextField pagesReadTextField;

    @FXML
    private TextField durationMinutesTextField;

    @FXML
    private Button saveButton;

    public void setEditMode(ReadingSession session) {
        model.setEditMode(session);
        bindModel();
    }

    @FXML
    void initialize() {
        // readers
        readerComboBox.getItems().setAll(readerDao.findAll());
        readerComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Reader reader) {
                return reader == null ? "" : reader.getName();
            }

            @Override
            public Reader fromString(String string) {
                return readerComboBox.getItems().stream()
                        .filter(r -> r.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });

        // books
        bookComboBox.getItems().setAll(bookDao.findAll());
        bookComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Book book) {
                return book == null ? "" : book.getTitle();
            }

            @Override
            public Book fromString(String string) {
                return bookComboBox.getItems().stream()
                        .filter(b -> b.getTitle().equals(string))
                        .findFirst().orElse(null);
            }
        });

        bindModel();
    }

    private void bindModel() {
        datePicker.valueProperty().bindBidirectional(model.dateProperty());
        readerComboBox.valueProperty().bindBidirectional(model.readerProperty());
        bookComboBox.valueProperty().bindBidirectional(model.bookProperty());

        pagesReadTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int v = Integer.parseInt(newVal);
                model.pagesReadProperty().set(v);
            } catch (NumberFormatException e) {
                // простий варіант: ігноруємо
            }
        });

        durationMinutesTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int v = Integer.parseInt(newVal);
                model.durationMinutesProperty().set(v);
            } catch (NumberFormatException e) {
                // ігноруємо
            }
        });
    }

    @FXML
    void saveButtonAction(ActionEvent event) {
        ReadingSession session = model.toReadingSession();
        if (model.isEditMode()) {
            readingSessionDao.update(session);
        } else {
            readingSessionDao.create(session);
        }
        saveButton.getScene().getWindow().hide();
    }
}
