package booklib;

import booklib.books.BookDao;
import booklib.books.MysqlBookDao;
import booklib.readers.MysqlReaderDao;
import booklib.readers.ReaderDao;
import booklib.readingSessions.MysqlReadingSessionDao;
import booklib.readingSessions.ReadingSessionDao;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

public enum Factory {
    INSTANCE;

    private final Object lock = new Object();

    private volatile JdbcOperations jdbcOperations;

    private volatile BookDao bookDao;
    private volatile ReaderDao readerDao;
    private volatile ReadingSessionDao readingSessionDao;

    public JdbcOperations getMysqlJdbcOperations() {
        if (jdbcOperations == null) {
            synchronized (lock) {
                if (jdbcOperations == null) {
                    MysqlDataSource ds = new MysqlDataSource();

                    ds.setURL("jdbc:mysql://localhost:3307/bookLib" +
                            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                    ds.setUser("bookLib");
                    ds.setPassword("bookLib");

                    jdbcOperations = new JdbcTemplate(ds);
                }
            }
        }
        return jdbcOperations;
    }

    public BookDao getBookDao() {
        if (bookDao == null) {
            synchronized (lock) {
                if (bookDao == null) {
                    bookDao = new MysqlBookDao(getMysqlJdbcOperations());
                }
            }
        }
        return bookDao;
    }

    public ReaderDao getReaderDao() {
        if (readerDao == null) {
            synchronized (lock) {
                if (readerDao == null) {
                    readerDao = new MysqlReaderDao(getMysqlJdbcOperations());
                }
            }
        }
        return readerDao;
    }

    public ReadingSessionDao getReadingSessionDao() {
        if (readingSessionDao == null) {
            synchronized (lock) {
                if (readingSessionDao == null) {
                    readingSessionDao = new MysqlReadingSessionDao(getMysqlJdbcOperations());
                }
            }
        }
        return readingSessionDao;
    }
}
