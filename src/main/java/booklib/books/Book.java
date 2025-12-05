package booklib.books;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Data
public class Book {
    private Long id;
    private String title;
    private Integer pages;
    private String genre;
    private String language;
    private LocalDateTime createdAt;

    public static Book fromResultSet(ResultSet rs) throws SQLException {
        return fromResultSet(rs, "");
    }

    public static Book fromResultSet(ResultSet rs, String aliasPrefix) throws SQLException {
        // 1. Прочитать ID и проверить на NULL
        Long id = rs.getLong(aliasPrefix + "id");
        if (rs.wasNull()) {
            return null;
        }

        // 2. Создаём объект Book
        Book book = new Book();
        book.setId(id);

        // 3. Простые поля
        book.setTitle(rs.getString(aliasPrefix + "title"));
        book.setPages(rs.getInt(aliasPrefix + "pages"));
        if (rs.wasNull()) book.setPages(null);

        book.setGenre(rs.getString(aliasPrefix + "genre"));
        book.setLanguage(rs.getString(aliasPrefix + "language"));

        // 4. TIMESTAMP → LocalDateTime
        var ts = rs.getTimestamp(aliasPrefix + "created_at");
        if (ts != null) {
            book.setCreatedAt(ts.toLocalDateTime());
        }

        return book;
    }
}
