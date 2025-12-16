package booklib.books;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Data
public class Book {
    private Long id;
    private String title;
    private String author;
    private Integer pages;
    private String genre;
    private String language;
    private LocalDateTime createdAt;

    // NEW: status from book_status (not from book table)
    private String status;

    public static Book fromResultSet(ResultSet rs) throws SQLException {
        return fromResultSet(rs, "");
    }

    public static Book fromResultSet(ResultSet rs, String aliasPrefix) throws SQLException {
        if (rs == null) return null;
        if (aliasPrefix == null) aliasPrefix = "";

        var book = new Book();

        long id = rs.getLong(aliasPrefix + "id");
        if (rs.wasNull()) return null;
        book.setId(id);

        book.setTitle(rs.getString(aliasPrefix + "title"));
        book.setAuthor(rs.getString(aliasPrefix + "author"));
        book.setGenre(rs.getString(aliasPrefix + "genre"));
        book.setLanguage(rs.getString(aliasPrefix + "language"));

        int pages = rs.getInt(aliasPrefix + "pages");
        book.setPages(rs.wasNull() ? null : pages);

        var ts = rs.getTimestamp(aliasPrefix + "created_at");
        if (ts != null) book.setCreatedAt(ts.toLocalDateTime());

        // If query selects status as "status", fill it; otherwise ignore.
        try {
            book.setStatus(rs.getString("status"));
        } catch (SQLException ignored) {
            // query didn't select status column => leave null
        }

        return book;
    }
}
