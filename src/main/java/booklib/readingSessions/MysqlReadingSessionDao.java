package booklib.readingSessions;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import booklib.books.Book;
import booklib.exceptions.NotFoundException;
import booklib.readers.Reader;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MysqlReadingSessionDao implements ReadingSessionDao {

    private final JdbcOperations jdbcOperations;

    private final ResultSetExtractor<List<ReadingSession>> resultSetExtractor = rs -> {
        var sessions = new ArrayList<ReadingSession>();
        var processedSessions = new HashMap<Long, ReadingSession>();
        var processedReaders = new HashMap<Long, Reader>();
        var processedBooks = new HashMap<Long, Book>();

        while (rs.next()) {
            long id = rs.getLong("rs_id");
            var session = processedSessions.get(id);
            if (session == null) {
                session = ReadingSession.fromResultSet(rs, "rs_");
                processedSessions.put(id, session);
                sessions.add(session);
            }

            long readerId = rs.getLong("r_id");
            Reader reader = processedReaders.get(readerId);
            if (reader == null) {
                reader = Reader.fromResultSet(rs, "r_");
                processedReaders.put(readerId, reader);
            }

            long bookId = rs.getLong("b_id");
            Book book = processedBooks.get(bookId);
            if (book == null) {
                book = Book.fromResultSet(rs, "b_");
                processedBooks.put(bookId, book);
            }

            session.setReader(reader);
            session.setBook(book);
        }

        return sessions;
    };

    private static final String SELECT_QUERY =
            "SELECT " +
                    "rs.id AS rs_id, rs.pages_read AS rs_pages_read, rs.duration_minutes AS rs_duration_minutes, rs.created_at AS rs_created_at, " +
                    "r.id AS r_id, r.name AS r_name, r.password_hash AS r_password_hash, r.created_at AS r_created_at, " +
                    "b.id AS b_id, b.title AS b_title, b.pages AS b_pages, b.genre AS b_genre, b.language AS b_language, b.created_at AS b_created_at " +
                    "FROM reading_session rs " +
                    "JOIN reader r ON rs.reader_id = r.id " +
                    "JOIN book b ON rs.book_id = b.id";

    public MysqlReadingSessionDao(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public List<ReadingSession> findAll() {
        return jdbcOperations.query(SELECT_QUERY, resultSetExtractor);
    }

    @Override
    public List<ReadingSession> findAllSortedByDate() {
        var query = SELECT_QUERY + " ORDER BY rs.created_at DESC";
        return jdbcOperations.query(query, resultSetExtractor);
    }

    public ReadingSession findById(Long id) {
        var query = SELECT_QUERY + " WHERE rs.id = ?";
        var sessions = jdbcOperations.query(query, resultSetExtractor, id);
        if (sessions == null || sessions.isEmpty()) {
            throw new NotFoundException("Reading session with id " + id + " not found");
        }
        return sessions.get(0);
    }

    @Override
    public ReadingSession create(ReadingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("ReadingSession is null");
        }
        if (session.getId() != null) {
            throw new IllegalArgumentException("ReadingSession id must be null for create");
        }

        var keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO reading_session (reader_id, book_id, pages_read, duration_minutes) VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, session.getReader().getId());
            ps.setLong(2, session.getBook().getId());
            ps.setInt(3, session.getPagesRead());
            ps.setInt(4, session.getDurationMinutes());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return findById(id);
    }

    @Override
    public ReadingSession update(ReadingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("ReadingSession is null");
        }
        if (session.getId() == null) {
            throw new IllegalArgumentException("ReadingSession id is null for update");
        }

        jdbcOperations.update(
                "UPDATE reading_session SET reader_id = ?, book_id = ?, pages_read = ?, duration_minutes = ? WHERE id = ?",
                session.getReader().getId(),
                session.getBook().getId(),
                session.getPagesRead(),
                session.getDurationMinutes(),
                session.getId()
        );

        return findById(session.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcOperations.update("DELETE FROM reading_session WHERE id = ?", id);
    }
}
