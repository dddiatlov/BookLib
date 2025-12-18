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

/**
 * ПОЛНОЦЕННАЯ РЕАЛИЗАЦИЯ BookDao ДЛЯ MySQL
 * Использует Spring JDBC для работы с базой данных
 * Особенности:
 * - Поддерживает все связи между таблицами
 * - Использует транзакции (через Spring)
 * - Генерирует ID через auto_increment
 * - Реализует все методы интерфейса
 */
public class MysqlBookDao implements BookDao {

    // Spring JdbcTemplate для выполнения SQL запросов
    private final JdbcOperations jdbcOperations;

    /**
     * ЭКСТРАКТОР ДАННЫХ
     * Преобразует ResultSet (результат SQL запроса) в List<Book>
     * Используется в методах findAll() и findById()
     */
    private final ResultSetExtractor<List<Book>> bookExtractor = rs -> {
        var books = new ArrayList<Book>();
        while (rs.next()) {
            var book = Book.fromResultSet(rs);
            if (book != null) books.add(book);
        }
        return books;
    };

    // SQL запрос для получения всех книг
    private static final String SELECT_ALL =
            "SELECT id, title, author, pages, genre, language, created_at FROM book";

    /**
     * Конструктор с внедрением зависимости (Dependency Injection)
     * @param jdbcOperations обычно это JdbcTemplate из Spring
     */
    public MysqlBookDao(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * ЗАГРУЗКА ИЗ CSV С ИСПОЛЬЗОВАНИЕМ MemoryBookDao
     * Алгоритм:
     * 1. Загружаем CSV в память через MemoryBookDao
     * 2. Сохраняем каждую книгу в MySQL
     * 3. Используем INSERT ... ON DUPLICATE KEY UPDATE для обновления существующих книг
     */
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

    /**
     * ДОБАВЛЕНИЕ НОВОЙ КНИГИ С ГЕНЕРАЦИЕЙ ID
     * Использует GeneratedKeyHolder для получения сгенерированного ID
     * Устанавливает текущее время, если createdAt не указан
     */
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

        // Получаем сгенерированный ID и устанавливаем его в объект
        Number key = keyHolder.getKey();
        if (key != null) {
            book.setId(key.longValue());
        } else {
            throw new IllegalStateException("Failed to obtain generated book id.");
        }
    }

    /**
     * ПОЛУЧЕНИЕ ВСЕХ КНИГ (без статусов)
     */
    @Override
    public List<Book> findAll() {
        return jdbcOperations.query(SELECT_ALL, bookExtractor);
    }

    /**
     * ПОИСК КНИГИ ПО ID
     */
    @Override
    public Book findById(Long id) {
        var list = jdbcOperations.query(
                "SELECT id, title, author, pages, genre, language, created_at FROM book WHERE id = ?",
                bookExtractor,
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * НАХОЖДЕНИЕ КНИГ КОНКРЕТНОГО ЧИТАТЕЛЯ (МОИ КНИГИ)
     * JOIN между book и book_status
     * Возвращает книги со статусами (поле status заполняется)
     */
    @Override
    public List<Book> findByReaderId(Long readerId) {
        String sql =
                "SELECT " +
                        "b.id, b.title, b.author, b.pages, b.genre, b.language, b.created_at, " +
                        "bs.status AS bs_status " +  // Алиас для колонки status
                        "FROM book b " +
                        "JOIN book_status bs ON bs.book_id = b.id " +
                        "WHERE bs.reader_id = ? " +
                        "ORDER BY bs.created_at DESC";

        return jdbcOperations.query(sql, (rs, rowNum) -> {
            Book book = Book.fromResultSet(rs);     // читает основные поля книги
            book.setStatus(rs.getString("bs_status")); // явно устанавливаем статус
            return book;
        }, readerId);
    }

    /**
     * ДОБАВЛЕНИЕ КНИГИ ДЛЯ ЧИТАТЕЛЯ (в book_status)
     * Использует ON DUPLICATE KEY UPDATE для обновления статуса, если связь уже существует
     */
    @Override
    public void addBookForReader(Long bookId, Long readerId, String status) {
        jdbcOperations.update(
                "INSERT INTO book_status (book_id, reader_id, status, created_at) " +
                        "VALUES (?, ?, ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE status=VALUES(status)",
                bookId, readerId, status
        );
    }

    /**
     * УДАЛЕНИЕ КНИГИ ИЗ "МОИХ КНИГ"
     */
    @Override
    public void removeBookForReader(Long bookId, Long readerId) {
        jdbcOperations.update(
                "DELETE FROM book_status WHERE book_id = ? AND reader_id = ?",
                bookId, readerId
        );
    }

    // ========== МЕТОДЫ ДЛЯ ИЗБРАННЫХ КНИГ ==========

    /**
     * ПРОВЕРКА, ЯВЛЯЕТСЯ ЛИ КНИГА ИЗБРАННОЙ
     */
    @Override
    public boolean isFavorite(long readerId, long bookId) {
        Integer cnt = jdbcOperations.queryForObject(
                "SELECT COUNT(*) FROM favorite_books WHERE reader_id = ? AND book_id = ?",
                Integer.class,
                readerId, bookId
        );
        return cnt != null && cnt > 0;
    }

    /**
     * ДОБАВЛЕНИЕ В ИЗБРАННОЕ
     * IGNORE предотвращает ошибку при повторном добавлении
     */
    @Override
    public void addFavorite(long readerId, long bookId) {
        jdbcOperations.update(
                "INSERT IGNORE INTO favorite_books (reader_id, book_id) VALUES (?, ?)",
                readerId, bookId
        );
    }

    /**
     * УДАЛЕНИЕ ИЗ ИЗБРАННОГО
     */
    @Override
    public void removeFavorite(long readerId, long bookId) {
        jdbcOperations.update(
                "DELETE FROM favorite_books WHERE reader_id = ? AND book_id = ?",
                readerId, bookId
        );
    }

    /**
     * ПОЛУЧЕНИЕ ВСЕХ ИЗБРАННЫХ КНИГ ЧИТАТЕЛЯ
     * LEFT JOIN с book_status для получения статуса чтения (если книга в "Моих книгах")
     */
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
            Book book = Book.fromResultSet(rs);              // основные поля книги
            book.setStatus(rs.getString("bs_status"));       // статус (может быть null)
            return book;
        }, readerId);
    }
}