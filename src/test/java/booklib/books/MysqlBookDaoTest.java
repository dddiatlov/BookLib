package booklib.books;

import booklib.dao.TestDb;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MysqlBookDaoTest {

    static JdbcTemplate jdbc;
    MysqlBookDao dao;

    @BeforeAll
    static void startDb() {
        jdbc = TestDb.jdbc();
        TestDb.runSchema(jdbc);
    }


    @BeforeEach
    void setUp() {
        // чистим в правильном порядке из-за FK
        jdbc.update("DELETE FROM reading_session");
        jdbc.update("DELETE FROM book_status");
        jdbc.update("DELETE FROM favorite_books");
        jdbc.update("DELETE FROM book");
        jdbc.update("DELETE FROM reader");

        dao = new MysqlBookDao(jdbc);
    }

    // ---------- helpers ----------
    private long insertReader(String name) {
        jdbc.update("INSERT INTO reader(name, password_hash) VALUES(?,?)", name, "h");
        Long id = jdbc.queryForObject("SELECT id FROM reader WHERE name=?", Long.class, name);
        assertNotNull(id);
        return id;
    }

    private long insertBook(String title) {
        jdbc.update(
                "INSERT INTO book(title, author, pages, genre, language, created_at) VALUES(?,?,?,?,?,NOW())",
                title, "author", 100, "genre", "en"
        );
        Long id = jdbc.queryForObject("SELECT id FROM book WHERE title=?", Long.class, title);
        assertNotNull(id);
        return id;
    }

    // ---------- tests ----------

    @Test
    void add_shouldGenerateId_andFindById() {
        Book b = new Book();
        b.setTitle("Title");
        b.setAuthor("Author");
        b.setPages(123);
        b.setGenre("Drama");
        b.setLanguage("en");
        b.setCreatedAt(LocalDateTime.now());

        dao.add(b);
        assertNotNull(b.getId());

        Book found = dao.findById(b.getId());
        assertNotNull(found);
        assertEquals("Title", found.getTitle());
        assertEquals("Author", found.getAuthor());
        assertEquals(123, found.getPages());
        assertEquals("Drama", found.getGenre());
    }

    @Test
    void add_null_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> dao.add(null));
    }

    @Test
    void findAll_shouldReturnEmpty_thenNonEmpty() {
        assertEquals(0, dao.findAll().size());

        insertBook("b1");
        insertBook("b2");

        assertEquals(2, dao.findAll().size());
    }

    @Test
    void findById_notExisting_shouldReturnNull() {
        assertNull(dao.findById(999999L));
    }

    @Test
    void addBookForReader_and_findByReaderId_shouldReturnBooksWithStatus() {
        long readerId = insertReader("r1");
        long book1 = insertBook("b1");
        long book2 = insertBook("b2");

        dao.addBookForReader(book1, readerId, "READING");
        dao.addBookForReader(book2, readerId, "FINISHED");

        var list = dao.findByReaderId(readerId);
        assertEquals(2, list.size());

        assertTrue(list.stream().anyMatch(b -> b.getId().equals(book1) && "READING".equals(b.getStatus())));
        assertTrue(list.stream().anyMatch(b -> b.getId().equals(book2) && "FINISHED".equals(b.getStatus())));
    }

    @Test
    void removeBookForReader_shouldRemoveOnlyThatLink() {
        long readerId = insertReader("r1");
        long book1 = insertBook("b1");
        long book2 = insertBook("b2");

        dao.addBookForReader(book1, readerId, "READING");
        dao.addBookForReader(book2, readerId, "FINISHED");

        dao.removeBookForReader(book1, readerId);

        var list = dao.findByReaderId(readerId);
        assertEquals(1, list.size());
        assertEquals(book2, list.get(0).getId());
    }

    @Test
    void favorites_add_isFavorite_findFavorites_remove() {
        long readerId = insertReader("r1");
        long book1 = insertBook("b1");
        long book2 = insertBook("b2");

        assertFalse(dao.isFavorite(readerId, book1));

        dao.addFavorite(readerId, book1);
        dao.addFavorite(readerId, book2);

        assertTrue(dao.isFavorite(readerId, book1));
        assertTrue(dao.isFavorite(readerId, book2));

        var favs = dao.findFavoritesByReaderId(readerId);
        assertEquals(2, favs.size());

        dao.removeFavorite(readerId, book1);
        assertFalse(dao.isFavorite(readerId, book1));
        assertTrue(dao.isFavorite(readerId, book2));
    }

    @Test
    void findFavorites_shouldIncludeStatusIfPresent() {
        long readerId = insertReader("r1");
        long book1 = insertBook("b1");

        // добавили в избранное
        dao.addFavorite(readerId, book1);

        // добавили в "мои книги" со статусом
        dao.addBookForReader(book1, readerId, "READING");

        var favs = dao.findFavoritesByReaderId(readerId);
        assertEquals(1, favs.size());
        assertEquals("READING", favs.get(0).getStatus());
    }

    @Test
    void loadFromCsv_smokeTest() throws Exception {
        // ⚠️ Это "smoke test": если формат CSV у тебя другой — подгони содержимое файла под MemoryBookDao.
        // Главное: метод должен отработать и данные должны появиться в таблице book.

        File tmp = Files.createTempFile("books", ".csv").toFile();
        tmp.deleteOnExit();

        // Популярный вариант: CSV с заголовком, разделитель запятая.
        // Если у тебя ; или другой порядок — поменяй строку.
        String csv =
                "id,title,author,pages,genre,language,createdAt\n" +
                "1,CSV Book,CSV Author,111,Drama,en,2025-01-01T10:00:00\n";
        Files.writeString(tmp.toPath(), csv);

        int loaded = dao.loadFromCsv(tmp);

        assertTrue(loaded >= 0);
        // хотя бы одна книга должна быть
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM book", Integer.class) >= 1);
    }
}
