package booklib;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import booklib.books.BookDao;
import booklib.books.MysqlBookDao;
import booklib.readers.MysqlReaderDao;
import booklib.readers.ReaderDao;
import booklib.readingSessions.MysqlReadingSessionDao;
import booklib.readingSessions.ReadingSessionDao;

public enum Factory {
    INSTANCE;

    private volatile JdbcOperations jdbcOperations;
    private volatile ReaderDao readerDao;
    private volatile BookDao bookDao;
    private volatile ReadingSessionDao readingSessionDao;

    private final Object lock = new Object();

    public JdbcOperations getMysqlJdbcOperations() {
        if (jdbcOperations == null) {
            synchronized (lock) {
                if (jdbcOperations == null) {
                    var dataSource = new MysqlDataSource();
                    dataSource.setUrl(System.getProperty("DB_JDBC", "jdbc:mysql://localhost:3306/bookLib"));
                    dataSource.setUser(System.getProperty("DB_USER", "bookLib"));
                    dataSource.setPassword(System.getProperty("DB_PASSWORD", "bookLib"));
                    jdbcOperations = new JdbcTemplate(dataSource);
                }
            }
        }
        return jdbcOperations;
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
