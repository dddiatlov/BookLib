package booklib.readingSessions;

import javafx.beans.property.*;
import booklib.books.Book;
import booklib.readers.Reader;

import java.time.LocalDate;

public class ReadingSessionModel {

    private Long id = null;

    private final ObjectProperty<LocalDate> dateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Reader> readerProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Book> bookProperty = new SimpleObjectProperty<>();
    private final IntegerProperty pagesReadProperty = new SimpleIntegerProperty();
    private final IntegerProperty durationMinutesProperty = new SimpleIntegerProperty();

    public void setEditMode(ReadingSession session) {
        this.id = session.getId();
        dateProperty.set(session.getCreatedAt() != null ? session.getCreatedAt().toLocalDate() : LocalDate.now());
        readerProperty.set(session.getReader());
        bookProperty.set(session.getBook());
        pagesReadProperty.set(session.getPagesRead());
        durationMinutesProperty.set(session.getDurationMinutes());
    }

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

    public ReadingSession toReadingSession() {
        ReadingSession session = new ReadingSession();
        session.setId(id);
        session.setReader(readerProperty.get());
        session.setBook(bookProperty.get());
        session.setPagesRead(pagesReadProperty.get());
        session.setDurationMinutes(durationMinutesProperty.get());
        session.setCreatedAt(null);
        return session;
    }

    public boolean isEditMode() {
        return id != null;
    }
}
