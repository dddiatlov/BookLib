package booklib.readingSessions;

import lombok.Data;
import booklib.books.Book;
import booklib.readers.Reader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Data
public class ReadingSession {
    private Long id;
    private Reader reader;
    private Book book;
    private int pagesRead;
    private int durationMinutes;
    private LocalDateTime createdAt;

    public static ReadingSession fromResultSet(ResultSet rs) throws SQLException {
        return fromResultSet(rs, "");
    }

    public static ReadingSession fromResultSet(ResultSet rs, String aliasPrefix) throws SQLException {
        long id = rs.getLong(aliasPrefix + "id");
        if (rs.wasNull()) {
            return null;
        }

        ReadingSession session = new ReadingSession();
        session.setId(id);
        session.setPagesRead(rs.getInt(aliasPrefix + "pages_read"));
        session.setDurationMinutes(rs.getInt(aliasPrefix + "duration_minutes"));
        var ts = rs.getTimestamp(aliasPrefix + "created_at");
        session.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        // reader і book будуть ставитися в ResultSetExtractor у DAO
        return session;
    }
}
