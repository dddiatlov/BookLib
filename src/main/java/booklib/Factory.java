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

/**
 * ФАБРИКА ДЛЯ СОЗДАНИЯ ОБЪЕКТОВ DAO И ПОДКЛЮЧЕНИЯ К БАЗЕ ДАННЫХ
 *
 * РЕАЛИЗУЕТ ПАТТЕРНЫ:
 * 1. Singleton - только один экземпляр фабрики
 * 2. Factory Method - создание конкретных реализаций DAO
 * 3. Double-Checked Locking - потокобезопасная ленивая инициализация
 *
 * ПРЕИМУЩЕСТВА:
 * - Централизованное управление зависимостями
 * - Ленивая инициализация (объекты создаются только при первом использовании)
 * - Потокобезопасность
 * - Легкая смена реализации (например, замена MySQL на другую БД)
 */
public enum Factory {
    INSTANCE;  // Реализация Singleton через enum (гарантирует один экземпляр)

    // Объект для синхронизации (используется в double-checked locking)
    private final Object lock = new Object();

    // Подключение к БД (Spring JdbcOperations)
    private volatile JdbcOperations jdbcOperations;

    // DAO объекты (используем volatile для корректной работы в многопоточной среде)
    private volatile BookDao bookDao;
    private volatile ReaderDao readerDao;
    private volatile ReadingSessionDao readingSessionDao;

    /**
     * СОЗДАНИЕ ИЛИ ПОЛУЧЕНИЕ ПОДКЛЮЧЕНИЯ К MYSQL
     *
     * Double-Checked Locking алгоритм:
     * 1. Первая проверка без блокировки (для производительности)
     * 2. Синхронизация для потокобезопасности
     * 3. Вторая проверка внутри synchronized
     */
    public JdbcOperations getMysqlJdbcOperations() {
        if (jdbcOperations == null) {
            synchronized (lock) {
                if (jdbcOperations == null) {
                    // Создаем DataSource для подключения к MySQL
                    MysqlDataSource ds = new MysqlDataSource();

                    // Параметры подключения к БД (соответствуют docker-compose.yml)
                    ds.setURL("jdbc:mysql://localhost:3307/bookLib" +
                            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                    ds.setUser("bookLib");      // Пользователь
                    ds.setPassword("bookLib");  // Пароль

                    // Создаем JdbcTemplate для работы с БД через Spring JDBC
                    jdbcOperations = new JdbcTemplate(ds);
                }
            }
        }
        return jdbcOperations;
    }

    /**
     * ФАБРИЧНЫЙ МЕТОД ДЛЯ СОЗДАНИЯ BookDao
     * Использует ту же схему double-checked locking
     */
    public BookDao getBookDao() {
        if (bookDao == null) {
            synchronized (lock) {
                if (bookDao == null) {
                    // Создаем DAO для книг, передавая подключение к БД
                    bookDao = new MysqlBookDao(getMysqlJdbcOperations());
                }
            }
        }
        return bookDao;
    }

    /**
     * ФАБРИЧНЫЙ МЕТОД ДЛЯ СОЗДАНИЯ ReaderDao
     */
    public ReaderDao getReaderDao() {
        if (readerDao == null) {
            synchronized (lock) {
                if (readerDao == null) {
                    // Создаем DAO для читателей
                    readerDao = new MysqlReaderDao(getMysqlJdbcOperations());
                }
            }
        }
        return readerDao;
    }

    /**
     * ФАБРИЧНЫЙ МЕТОД ДЛЯ СОЗДАНИЯ ReadingSessionDao
     */
    public ReadingSessionDao getReadingSessionDao() {
        if (readingSessionDao == null) {
            synchronized (lock) {
                if (readingSessionDao == null) {
                    // Создаем DAO для сессий чтения
                    readingSessionDao = new MysqlReadingSessionDao(getMysqlJdbcOperations());
                }
            }
        }
        return readingSessionDao;
    }
}