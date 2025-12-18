package booklib.readingSessions;

import booklib.books.Book;
import booklib.exceptions.NotFoundException;
import booklib.readers.Reader;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MysqlReadingSessionDao implements ReadingSessionDao {

    private final JdbcOperations jdbcOperations;

    private static final String SELECT_QUERY =
            "SELECT " +
                    "rs.id AS rs_id, rs.pages_read AS rs_pages_read, rs.duration_minutes AS rs_duration_minutes, rs.created_at AS rs_created_at, " +
                    "r.id AS r_id, r.name AS r_name, r.password_hash AS r_password_hash, r.created_at AS r_created_at, " +
                    "b.id AS b_id, b.title AS b_title, b.author AS b_author, b.pages AS b_pages, b.genre AS b_genre, b.language AS b_language, b.created_at AS b_created_at " +
                    "FROM reading_session rs " +
                    "JOIN reader r ON rs.reader_id = r.id " +
                    "JOIN book b ON rs.book_id = b.id";

    public MysqlReadingSessionDao(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

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
            var reader = processedReaders.get(readerId);
            if (reader == null) {
                reader = Reader.fromResultSet(rs, "r_");
                processedReaders.put(readerId, reader);
            }

            long bookId = rs.getLong("b_id");
            var book = processedBooks.get(bookId);
            if (book == null) {
                book = Book.fromResultSet(rs, "b_");
                processedBooks.put(bookId, book);
            }

            session.setReader(reader);
            session.setBook(book);
        }
        return sessions;
    };

    @Override
    public List<ReadingSession> findAll() {
        return jdbcOperations.query(SELECT_QUERY, resultSetExtractor);
    }

    @Override
    public List<ReadingSession> findAllSortedByDate() {
        var query = SELECT_QUERY + " ORDER BY rs.created_at DESC";
        return jdbcOperations.query(query, resultSetExtractor);
    }

    @Override
    public List<ReadingSession> findByReaderIdAndBookId(long readerId, long bookId) {
        var query = SELECT_QUERY + " WHERE rs.reader_id = ? AND rs.book_id = ? ORDER BY rs.created_at DESC";
        return jdbcOperations.query(query, resultSetExtractor, readerId, bookId);
    }

    @Override
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
        if (session == null) throw new IllegalArgumentException("ReadingSession is null");
        if (session.getId() != null) throw new IllegalArgumentException("ReadingSession id must be null for create");
        if (session.getReader() == null || session.getReader().getId() == null) throw new IllegalArgumentException("Reader is not set");
        if (session.getBook() == null || session.getBook().getId() == null) throw new IllegalArgumentException("Book is not set");

        var keyHolder = new GeneratedKeyHolder();

        jdbcOperations.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO reading_session (reader_id, book_id, pages_read, duration_minutes, created_at) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, session.getReader().getId());
            ps.setLong(2, session.getBook().getId());
            ps.setInt(3, session.getPagesRead());
            ps.setInt(4, session.getDurationMinutes());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(session.getCreatedAt()));
            return ps;
        }, keyHolder);

        session.setId(keyHolder.getKey().longValue());
        return session;
    }

    @Override
    public ReadingSession update(ReadingSession session) {
        if (session == null) throw new IllegalArgumentException("ReadingSession is null");
        if (session.getId() == null) throw new IllegalArgumentException("ReadingSession id is null for update");
        if (session.getReader() == null || session.getReader().getId() == null) throw new IllegalArgumentException("Reader is not set");
        if (session.getBook() == null || session.getBook().getId() == null) throw new IllegalArgumentException("Book is not set");

        int updated = jdbcOperations.update(
                "UPDATE reading_session SET reader_id=?, book_id=?, pages_read=?, duration_minutes=?, created_at=? WHERE id=?",
                session.getReader().getId(),
                session.getBook().getId(),
                session.getPagesRead(),
                session.getDurationMinutes(),
                java.sql.Timestamp.valueOf(session.getCreatedAt()),
                session.getId()
        );

        if (updated == 0) throw new NotFoundException("Reading session with id " + session.getId() + " not found");
        return session;
    }

    @Override
    public void delete(Long id) {
        int deleted = jdbcOperations.update("DELETE FROM reading_session WHERE id = ?", id);
        if (deleted == 0) throw new NotFoundException("Reading session with id " + id + " not found");
    }

    @Override
    public int sumPagesForReaderAndBook(long readerId, long bookId) {
        Integer sum = jdbcOperations.queryForObject(
                "SELECT COALESCE(SUM(pages_read), 0) FROM reading_session WHERE reader_id = ? AND book_id = ?",
                Integer.class,
                readerId,
                bookId
        );
        return sum == null ? 0 : sum;
    }
}
