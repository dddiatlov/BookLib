package booklib.books;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Statement;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MysqlBookDao implements BookDao {

    private final JdbcOperations jdbcOperations;

    private final ResultSetExtractor<List<Book>> bookExtractor = rs -> {
        var books = new ArrayList<Book>();
        while (rs.next()) {
            var book = Book.fromResultSet(rs);
            if (book != null) books.add(book);
        }
        return books;
    };

    private static final String SELECT_ALL =
            "SELECT id, title, author, pages, genre, language, created_at FROM book";

    public MysqlBookDao(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public int loadFromCsv(File file) {
        var memoryDao = new MemoryBookDao(new ArrayList<>());
        int loaded = memoryDao.loadFromCsv(file);

        for (var book : memoryDao.findAll()) {
            jdbcOperations.update(
                    "INSERT INTO book (id, title, author, pages, genre, language, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "title=VALUES(title), author=VALUES(author), pages=VALUES(pages), " +
                            "genre=VALUES(genre), language=VALUES(language), created_at=VALUES(created_at)",
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPages(),
                    book.getGenre(),
                    book.getLanguage(),
                    Timestamp.valueOf(book.getCreatedAt() != null ? book.getCreatedAt() : LocalDateTime.now())
            );
        }
        return loaded;
    }

    @Override
    public void add(Book book) {
        if (book == null) throw new IllegalArgumentException("Book is null");

        LocalDateTime createdAt = (book.getCreatedAt() != null) ? book.getCreatedAt() : LocalDateTime.now();
        book.setCreatedAt(createdAt);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcOperations.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO book (title, author, pages, genre, language, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setInt(3, book.getPages());
            ps.setString(4, book.getGenre());
            ps.setString(5, book.getLanguage());
            ps.setTimestamp(6, Timestamp.valueOf(createdAt));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            book.setId(key.longValue());
        } else {
            throw new IllegalStateException("Failed to obtain generated book id.");
        }
    }

    @Override
    public List<Book> findAll() {
        return jdbcOperations.query(SELECT_ALL, bookExtractor);
    }

    @Override
    public Book findById(Long id) {
        var list = jdbcOperations.query(
                "SELECT id, title, author, pages, genre, language, created_at FROM book WHERE id = ?",
                bookExtractor,
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Book> findByReaderId(Long readerId) {
        String sql =
                "SELECT " +
                        "b.id, b.title, b.author, b.pages, b.genre, b.language, b.created_at, " +
                        "bs.status AS bs_status " +
                        "FROM book b " +
                        "JOIN book_status bs ON bs.book_id = b.id " +
                        "WHERE bs.reader_id = ? " +
                        "ORDER BY bs.created_at DESC";

        return jdbcOperations.query(sql, (rs, rowNum) -> {
            Book book = Book.fromResultSet(rs);
            book.setStatus(rs.getString("bs_status"));
            return book;
        }, readerId);
    }

    @Override
    public void addBookForReader(Long bookId, Long readerId, String status) {
        jdbcOperations.update(
                "INSERT INTO book_status (book_id, reader_id, status, created_at) " +
                        "VALUES (?, ?, ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE status=VALUES(status)",
                bookId, readerId, status
        );
    }

    @Override
    public void removeBookForReader(Long bookId, Long readerId) {
        if (bookId == null || readerId == null) {
            throw new IllegalArgumentException("bookId/readerId is null");
        }

        jdbcOperations.update(
                "DELETE FROM reading_session WHERE book_id = ? AND reader_id = ?",
                bookId, readerId
        );

        jdbcOperations.update(
                "DELETE FROM favorite_books WHERE book_id = ? AND reader_id = ?",
                bookId, readerId
        );

        jdbcOperations.update(
                "DELETE FROM book_status WHERE book_id = ? AND reader_id = ?",
                bookId, readerId
        );
    }

    @Override
    public boolean isFavorite(long readerId, long bookId) {
        Integer cnt = jdbcOperations.queryForObject(
                "SELECT COUNT(*) FROM favorite_books WHERE reader_id = ? AND book_id = ?",
                Integer.class,
                readerId, bookId
        );
        return cnt != null && cnt > 0;
    }

    @Override
    public void addFavorite(long readerId, long bookId) {
        jdbcOperations.update(
                "INSERT IGNORE INTO favorite_books (reader_id, book_id) VALUES (?, ?)",
                readerId, bookId
        );
    }

    @Override
    public void removeFavorite(long readerId, long bookId) {
        jdbcOperations.update(
                "DELETE FROM favorite_books WHERE reader_id = ? AND book_id = ?",
                readerId, bookId
        );
    }

    @Override
    public List<Book> findFavoritesByReaderId(long readerId) {
        String sql = """
                SELECT
                    b.id, b.title, b.author, b.pages, b.genre, b.language, b.created_at,
                    bs.status AS bs_status
                FROM book b
                JOIN favorite_books fb ON fb.book_id = b.id
                LEFT JOIN book_status bs
                       ON bs.book_id = b.id AND bs.reader_id = fb.reader_id
                WHERE fb.reader_id = ?
                ORDER BY b.title
                """;

        return jdbcOperations.query(sql, (rs, rowNum) -> {
            Book book = Book.fromResultSet(rs);
            book.setStatus(rs.getString("bs_status"));
            return book;
        }, readerId);
    }
}
