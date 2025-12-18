package booklib.books;

import booklib.Alerts;
import booklib.Factory;
import booklib.Session;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

/**
 * КОНТРОЛЛЕР ОКНА "ДОБАВИТЬ КНИГУ"
 * Управляет диалоговым окном добавления книги в "Мои книги"
 * Паттерн MVC: Controller связывает View (FXML) с Model (BookDao)
 */
public class AddBookController {

    // Статус по умолчанию при добавлении книги
    private static final String STATUS = "WANT_TO_READ";

    // ЭЛЕМЕНТЫ УПРАВЛЕНИЯ ИЗ FXML (аннотация @FXML для связи с FXML)
    @FXML private ComboBox<Book> bookCombo;  // Выпадающий список книг
    @FXML private Label authorValue;         // Метка для автора
    @FXML private Label pagesValue;          // Метка для страниц
    @FXML private Label genreValue;          // Метка для жанра
    @FXML private Label languageValue;       // Метка для языка

    // DAO для работы с книгами (получаем через Factory - паттерн "Абстрактная фабрика")
    private final BookDao bookDao = Factory.INSTANCE.getBookDao();

    // Флаг успешного сохранения (используется вызывающей стороной)
    private boolean saved = false;
    public boolean isSaved() { return saved; }

    /**
     * МЕТОД ИНИЦИАЛИЗАЦИИ КОНТРОЛЛЕРА
     * Вызывается автоматически после загрузки FXML
     * 1. Загружает книги из базы
     * 2. Настраивает ComboBox
     * 3. Устанавливает обработчики событий
     */
    @FXML
    private void initialize() {
        // Получаем все книги из базы
        List<Book> all = bookDao.findAll();
        bookCombo.getItems().setAll(all);

        // Настройка отображения книг в ComboBox
        bookCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Book b) {
                if (b == null) return "";
                // Формат: "Название — Автор" (если автор есть)
                String a = (b.getAuthor() == null || b.getAuthor().isBlank()) ? "" : (" — " + b.getAuthor());
                return b.getTitle() + a;
            }
            @Override public Book fromString(String string) { return null; }
        });

        // Настройка отображения в выпадающем списке
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

        // Слушатель изменения выбранной книги
        bookCombo.valueProperty().addListener((obs, oldVal, newVal) -> showDetails(newVal));

        // Автоматически выбираем первую книгу, если они есть
        if (!all.isEmpty()) {
            bookCombo.getSelectionModel().select(0);
            showDetails(all.get(0));
        } else {
            // Если книг нет - показываем сообщение
            showDetails(null);
            Alerts.info("No books", "Database has no books yet. Use 'Import CSV' to load /booklib/books.csv.");
        }
    }

    /**
     * ОТОБРАЖЕНИЕ ДЕТАЛЕЙ ВЫБРАННОЙ КНИГИ
     * Обновляет правую часть окна с информацией о книге
     */
    private void showDetails(Book b) {
        authorValue.setText(b == null ? "—" : (b.getAuthor() == null || b.getAuthor().isBlank() ? "—" : b.getAuthor()));
        pagesValue.setText(b == null ? "—" : String.valueOf(b.getPages()));
        genreValue.setText(b == null ? "—" : (b.getGenre() == null || b.getGenre().isBlank() ? "—" : b.getGenre()));
        languageValue.setText(b == null ? "—" : (b.getLanguage() == null || b.getLanguage().isBlank() ? "—" : b.getLanguage()));
    }

    /**
     * ОБРАБОТЧИК КНОПКИ "SAVE"
     * 1. Проверяет входные данные
     * 2. Проверяет, не добавлена ли книга уже
     * 3. Создает связь в БД (book_status)
     * 4. Закрывает окно
     */
    @FXML
    public void onSave() {
        try {
            // Получаем ID текущего пользователя из сессии
            Long readerId = Long.valueOf(Session.requireReaderId());

            Book selected = bookCombo.getValue();
            if (selected == null || selected.getId() == null) {
                Alerts.error("Validation", "Please select a book from the list.");
                return;
            }

            // Проверка на дубликат (уже в "Моих книгах")
            List<Book> myBooks = bookDao.findByReaderId(readerId);
            boolean alreadyAdded = myBooks.stream()
                    .anyMatch(b -> b.getId() != null && b.getId().equals(selected.getId()));

            if (alreadyAdded) {
                Alerts.info("Already added", "This book is already in 'My Books'.");
                saved = false;
                bookCombo.requestFocus(); // Возвращаем фокус на выбор книги
                return;
            }

            // Добавляем связь книга-читатель в БД
            bookDao.addBookForReader(selected.getId(), readerId, STATUS);

            saved = true;
            close(); // Закрываем окно

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("Error", e.getMessage());
        }
    }

    /**
     * ОБРАБОТЧИК КНОПКИ "CANCEL"
     * Закрывает окно без сохранения
     */
    @FXML
    public void onCancel() {
        close();
    }

    /**
     * ЗАКРЫТИЕ ОКНА
     * Получает Stage из любого элемента управления
     */
    private void close() {
        Stage stage = (Stage) bookCombo.getScene().getWindow();
        stage.close();
    }
}