package booklib.readingSessions;

import javafx.beans.property.*;
import booklib.books.Book;
import booklib.readers.Reader;

import java.time.LocalDate;

/**
 * МОДЕЛЬ ДЛЯ ОТОБРАЖЕНИЯ СЕССИИ ЧТЕНИЯ В ПОЛЬЗОВАТЕЛЬСКОМ ИНТЕРФЕЙСЕ
 * Использует JavaFX свойства (Property) для двусторонней привязки данных.
 */
public class ReadingSessionModel {

    private Long id = null;

    private final ObjectProperty<LocalDate> dateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Reader> readerProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Book> bookProperty = new SimpleObjectProperty<>();
    private final IntegerProperty pagesReadProperty = new SimpleIntegerProperty();
    private final IntegerProperty durationMinutesProperty = new SimpleIntegerProperty();

    /**
     * Устанавливает режим редактирования, заполняя свойства из существующей сессии
     */
    public void setEditMode(ReadingSession session) {
        this.id = session.getId();
        dateProperty.set(session.getCreatedAt() != null ? session.getCreatedAt().toLocalDate() : LocalDate.now());
        readerProperty.set(session.getReader());
        bookProperty.set(session.getBook());
        pagesReadProperty.set(session.getPagesRead());
        durationMinutesProperty.set(session.getDurationMinutes());
    }

    // Геттеры для свойств (используются для привязки в FXML)

    public ObjectProperty<LocalDate> dateProperty() {
        return dateProperty;
    }

    public ObjectProperty<Reader> readerProperty() {
        return readerProperty;
    }

    public ObjectProperty<Book> bookProperty() {
        return bookProperty;
    }

    public IntegerProperty pagesReadProperty() {
        return pagesReadProperty;
    }

    public IntegerProperty durationMinutesProperty() {
        return durationMinutesProperty;
    }

    /**
     * Преобразует модель в объект ReadingSession для сохранения в БД
     */
    public ReadingSession toReadingSession() {
        ReadingSession session = new ReadingSession();
        session.setId(id);
        session.setReader(readerProperty.get());
        session.setBook(bookProperty.get());
        session.setPagesRead(pagesReadProperty.get());
        session.setDurationMinutes(durationMinutesProperty.get());
        // createdAt можно оставить null – БД сама поставит current_timestamp
        session.setCreatedAt(null);
        return session;
    }

    /**
     * Проверяет, находимся ли мы в режиме редактирования (есть id)
     */
    public boolean isEditMode() {
        return id != null;
    }
}