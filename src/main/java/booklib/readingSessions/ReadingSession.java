package booklib.readingSessions;

import booklib.books.Book;
import booklib.readers.Reader;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * МОДЕЛЬ СЕССИИ ЧТЕНИЯ (ReadingSession)
 * Представляет одну сессию чтения, связанную с книгой и читателем.
 * Соответствует таблице 'reading_session' в базе данных.
 */
@Data
public class ReadingSession {
    private Long id;                      // Уникальный идентификатор сессии
    private Reader reader;                // Читатель, который читал книгу
    private Book book;                    // Книга, которую читали
    private int pagesRead;                // Количество прочитанных страниц в эту сессию
    private int durationMinutes;          // Продолжительность чтения в минутах
    private LocalDateTime createdAt;      // Дата и время создания записи о сессии

    /**
     * Создает объект ReadingSession из ResultSet (стандартные имена колонок)
     */
    public static ReadingSession fromResultSet(ResultSet rs) throws SQLException {
        return fromResultSet(rs, "");
    }

    /**
     * Создает объект ReadingSession из ResultSet с поддержкой алиасов
     * @param aliasPrefix префикс для колонок (например, "rs_" для "rs.id")
     */
    public static ReadingSession fromResultSet(ResultSet rs, String aliasPrefix) throws SQLException {
        long id = rs.getLong(aliasPrefix + "id");
        if (rs.wasNull()) {
            return null; // Если ID null, возвращаем null
        }

        ReadingSession session = new ReadingSession();
        session.setId(id);
        session.setPagesRead(rs.getInt(aliasPrefix + "pages_read"));
        session.setDurationMinutes(rs.getInt(aliasPrefix + "duration_minutes"));
        var ts = rs.getTimestamp(aliasPrefix + "created_at");
        session.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return session;
    }

    /**
     * Переопределенный toString для удобного отображения в UI
     * Формат: "дата — название книги | количество страниц | продолжительность в минутах"
     */
    @Override
    public String toString() {
        String date = (createdAt != null) ? createdAt.toLocalDate().toString() : "-";
        String bookTitle = (book != null && book.getTitle() != null) ? book.getTitle() : "";
        String bookPart = bookTitle.isBlank() ? "" : (" — " + bookTitle);
        return date + bookPart + " | " + pagesRead + " pages | " + durationMinutes + " min";
    }
}