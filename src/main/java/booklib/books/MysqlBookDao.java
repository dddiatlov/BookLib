package booklib.books;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MysqlBookDao implements BookDao {

    private final JdbcOperations jdbcOperations;

    private final ResultSetExtractor<List<Book>> bookExtractor = rs -> {
        var books = new ArrayList<Book>();
        while (rs.next()) {
            var book = Book.fromResultSet(rs);
            if (book != null) {
                books.add(book);
            }
        }
        return books;
    };

    private static final String SELECT_ALL =
            "SELECT id, title, pages, genre, language, created_at FROM book";

    public MysqlBookDao(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public int loadFromCsv(File file) {
        var memoryDao = new MemoryBookDao(new ArrayList<>());
        int loaded = memoryDao.loadFromCsv(file);

        for (var book : memoryDao.findAll()) {
            jdbcOperations.update(
                "INSERT INTO book (id, title, pages, genre, language, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                book.getId(),
                book.getTitle(),
                book.getPages(),
                book.getGenre(),
                book.getLanguage(),
                book.getCreatedAt()
            );
        }

        return loaded;
    }

    @Override
    public List<Book> findAll() {
        return jdbcOperations.query(SELECT_ALL, bookExtractor);
    }

    @Override
    public Book findById(Long id) {
        String sql = "SELECT id, title, pages, genre, language, created_at FROM book WHERE id = ?";
        var list = jdbcOperations.query(sql, bookExtractor, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Book> findByReaderId(Long readerId) {
        String sql =
            "SELECT b.id, b.title, b.pages, b.genre, b.language, b.created_at " +
            "FROM book b " +
            "JOIN book_status bs ON bs.book_id = b.id " +
            "WHERE bs.reader_id = ?";

        return jdbcOperations.query(sql, bookExtractor, readerId);
    }

    @Override
    public void addBookForReader(Long bookId, Long readerId, String status) {
        String sql =
            "INSERT INTO book_status (book_id, reader_id, status, created_at) " +
            "VALUES (?, ?, ?, NOW())";

        jdbcOperations.update(sql, bookId, readerId, status);
    }
}
