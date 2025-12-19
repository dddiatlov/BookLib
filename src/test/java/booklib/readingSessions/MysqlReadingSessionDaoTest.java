package booklib.readingSessions;

import booklib.books.Book;
import booklib.dao.TestDb;
import booklib.exceptions.NotFoundException;
import booklib.readers.Reader;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MysqlReadingSessionDaoTest {

    static JdbcTemplate jdbc;
    MysqlReadingSessionDao dao;

    @BeforeAll
    static void startDb() {
        jdbc = TestDb.jdbc();
        TestDb.runSchema(jdbc);
    }

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM reading_session");
        jdbc.update("DELETE FROM book_status");
        jdbc.update("DELETE FROM favorite_books");
        jdbc.update("DELETE FROM book");
        jdbc.update("DELETE FROM reader");

        dao = new MysqlReadingSessionDao(jdbc);
    }

    private Reader mkReader(String name) {
        jdbc.update("INSERT INTO reader(name, password_hash) VALUES(?,?)", name, "h");
        Long id = jdbc.queryForObject("SELECT id FROM reader WHERE name=?", Long.class, name);
        assertNotNull(id);

        Reader r = new Reader();
        r.setId(id);
        r.setName(name);
        r.setPasswordHash("h");
        return r;
    }

    private Book mkBook(String title) {
        jdbc.update(
                "INSERT INTO book(title, author, pages, genre, language, created_at) VALUES(?,?,?,?,?,NOW())",
                title, "author", 100, "genre", "en"
        );
        Long id = jdbc.queryForObject("SELECT id FROM book WHERE title=?", Long.class, title);
        assertNotNull(id);

        Book b = new Book();
        b.setId(id);
        b.setTitle(title);
        b.setAuthor("author");
        b.setPages(100);
        b.setGenre("genre");
        b.setLanguage("en");
        return b;
    }

    @Test
    void create_and_findById() {
        Reader r = mkReader("r1");
        Book b = mkBook("b1");

        ReadingSession s = new ReadingSession();
        s.setId(null);
        s.setReader(r);
        s.setBook(b);
        s.setPagesRead(10);
        s.setDurationMinutes(15);
        s.setCreatedAt(LocalDateTime.now());

        ReadingSession created = dao.create(s);

        assertNotNull(created.getId());

        ReadingSession found = dao.findById(created.getId());
        assertEquals(10, found.getPagesRead());
        assertEquals(15, found.getDurationMinutes());
        assertNotNull(found.getReader());
        assertNotNull(found.getBook());
        assertEquals(r.getId(), found.getReader().getId());
        assertEquals(b.getId(), found.getBook().getId());
    }

    @Test
    void findById_notExisting_shouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> dao.findById(999999L));
    }

    @Test
    void update_shouldPersistChanges() {
        Reader r = mkReader("r1");
        Book b = mkBook("b1");

        ReadingSession s = new ReadingSession();
        s.setReader(r);
        s.setBook(b);
        s.setPagesRead(5);
        s.setDurationMinutes(10);
        s.setCreatedAt(LocalDateTime.now().minusDays(1));

        dao.create(s);

        s.setPagesRead(50);
        s.setDurationMinutes(99);
        s.setCreatedAt(LocalDateTime.now());
        dao.update(s);

        ReadingSession found = dao.findById(s.getId());
        assertEquals(50, found.getPagesRead());
        assertEquals(99, found.getDurationMinutes());
    }

    @Test
    void update_notExisting_shouldThrowNotFound() {
        Reader r = mkReader("r1");
        Book b = mkBook("b1");

        ReadingSession s = new ReadingSession();
        s.setId(999999L);
        s.setReader(r);
        s.setBook(b);
        s.setPagesRead(1);
        s.setDurationMinutes(1);
        s.setCreatedAt(LocalDateTime.now());

        assertThrows(NotFoundException.class, () -> dao.update(s));
    }

    @Test
    void delete_shouldRemove_and_deleteNotExisting_shouldThrow() {
        Reader r = mkReader("r1");
        Book b = mkBook("b1");

        ReadingSession s = new ReadingSession();
        s.setReader(r);
        s.setBook(b);
        s.setPagesRead(10);
        s.setDurationMinutes(10);
        s.setCreatedAt(LocalDateTime.now());

        dao.create(s);

        dao.delete(s.getId());
        assertThrows(NotFoundException.class, () -> dao.findById(s.getId()));

        assertThrows(NotFoundException.class, () -> dao.delete(999999L));
    }

    @Test
    void sumPagesForReaderAndBook_shouldReturn0_thenSum() {
        Reader r = mkReader("r1");
        Book b = mkBook("b1");

        assertEquals(0, dao.sumPagesForReaderAndBook(r.getId(), b.getId()));

        ReadingSession s1 = new ReadingSession();
        s1.setReader(r);
        s1.setBook(b);
        s1.setPagesRead(10);
        s1.setDurationMinutes(5);
        s1.setCreatedAt(LocalDateTime.now().minusHours(2));
        dao.create(s1);

        ReadingSession s2 = new ReadingSession();
        s2.setReader(r);
        s2.setBook(b);
        s2.setPagesRead(20);
        s2.setDurationMinutes(10);
        s2.setCreatedAt(LocalDateTime.now().minusHours(1));
        dao.create(s2);

        assertEquals(30, dao.sumPagesForReaderAndBook(r.getId(), b.getId()));
    }

    @Test
    void findAll_and_findAllSortedByDate_and_findByReaderIdAndBookId() {
        Reader r1 = mkReader("r1");
        Reader r2 = mkReader("r2");
        Book b1 = mkBook("b1");
        Book b2 = mkBook("b2");

        ReadingSession s1 = new ReadingSession();
        s1.setReader(r1); s1.setBook(b1);
        s1.setPagesRead(1); s1.setDurationMinutes(1);
        s1.setCreatedAt(LocalDateTime.now().minusDays(2));
        dao.create(s1);

        ReadingSession s2 = new ReadingSession();
        s2.setReader(r1); s2.setBook(b1);
        s2.setPagesRead(2); s2.setDurationMinutes(2);
        s2.setCreatedAt(LocalDateTime.now().minusDays(1));
        dao.create(s2);

        ReadingSession s3 = new ReadingSession();
        s3.setReader(r2); s3.setBook(b2);
        s3.setPagesRead(3); s3.setDurationMinutes(3);
        s3.setCreatedAt(LocalDateTime.now());
        dao.create(s3);

        assertEquals(3, dao.findAll().size());

        var sorted = dao.findAllSortedByDate();
        assertEquals(3, sorted.size());
        assertTrue(sorted.get(0).getCreatedAt().isAfter(sorted.get(1).getCreatedAt())
                || sorted.get(0).getCreatedAt().isEqual(sorted.get(1).getCreatedAt()));

        var r1b1 = dao.findByReaderIdAndBookId(r1.getId(), b1.getId());
        assertEquals(2, r1b1.size());
        assertEquals(r1.getId(), r1b1.get(0).getReader().getId());
        assertEquals(b1.getId(), r1b1.get(0).getBook().getId());
    }

    @Test
    void create_validation_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.create(null));

        ReadingSession s = new ReadingSession();
        s.setId(1L);
        assertThrows(IllegalArgumentException.class, () -> dao.create(s));
    }
}
