package booklib.readers;

import booklib.dao.TestDb;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

class MysqlReaderDaoTest {

    static JdbcTemplate jdbc;
    MysqlReaderDao dao;

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

        dao = new MysqlReaderDao(jdbc);
    }

    @Test
    void save_and_findById() {
        Reader r = new Reader();
        r.setName("denis");
        r.setPasswordHash("hash");

        Reader saved = dao.save(r);
        assertNotNull(saved.getId());

        Reader found = dao.findById(saved.getId());
        assertEquals("denis", found.getName());
    }
}
