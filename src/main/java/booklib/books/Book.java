package booklib.books;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * МОДЕЛЬ КНИГИ (Entity)
 * Соответствует таблице 'book' в базе данных.
 * Аннотация @Data от Lombok генерирует:
 * - геттеры/сеттеры для всех полей
 * - equals(), hashCode(), toString()
 */
@Data
public class Book {
    private Long id;                  // Уникальный идентификатор (первичный ключ)
    private String title;             // Название книги
    private String author;            // Автор
    private Integer pages;            // Количество страниц (может быть null)
    private String genre;             // Жанр (например, "Фэнтези")
    private String language;          // Язык книги
    private LocalDateTime createdAt;  // Дата создания записи в БД

    /**
     * ВНИМАНИЕ: Это поле НЕ из таблицы book!
     * Это статус книги для конкретного пользователя из таблицы book_status
     * Например: "WANT_TO_READ", "READING", "FINISHED"
     */
    private String status;

    /**
     * СТАТИЧЕСКИЕ МЕТОДЫ-КОНСТРУКТОРЫ
     * Используются для преобразования данных из БД (ResultSet) в объекты Java
     * Это часть паттерна "RowMapper" в Spring JDBC
     */

    /**
     * Создает Book из ResultSet (колонки без префикса)
     */
    public static Book fromResultSet(ResultSet rs) throws SQLException {
        return fromResultSet(rs, "");
    }

    /**
     * Создает Book из ResultSet с поддержкой алиасов
     * @param aliasPrefix префикс колонок (например "b." для "b.id", "b.title")
     * Используется в JOIN запросах, где есть несколько таблиц
     */
    public static Book fromResultSet(ResultSet rs, String aliasPrefix) throws SQLException {
        if (rs == null) return null;
        if (aliasPrefix == null) aliasPrefix = "";

        Book book = new Book();

        // Чтение ID с проверкой на NULL (если ID null - книга не существует)
        long id = rs.getLong(aliasPrefix + "id");
        if (rs.wasNull()) return null;
        book.setId(id);

        // Чтение строковых полей
        book.setTitle(rs.getString(aliasPrefix + "title"));
        book.setAuthor(rs.getString(aliasPrefix + "author"));
        book.setGenre(rs.getString(aliasPrefix + "genre"));
        book.setLanguage(rs.getString(aliasPrefix + "language"));

        // Особенность: pages может быть NULL в БД
        int pages = rs.getInt(aliasPrefix + "pages");
        book.setPages(rs.wasNull() ? null : pages);

        // Преобразование Timestamp в LocalDateTime
        var ts = rs.getTimestamp(aliasPrefix + "created_at");
        if (ts != null) book.setCreatedAt(ts.toLocalDateTime());

        /**
         * ЛОВИМ ИСКЛЮЧЕНИЕ: статус есть не во всех запросах
         * Если в SQL запросе нет колонки "status" - игнорируем
         */
        try {
            book.setStatus(rs.getString("status"));
        } catch (SQLException ignored) {
            // Колонки "status" нет в результате запроса - оставляем null
        }

        return book;
    }
}
