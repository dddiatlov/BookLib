package booklib.readers;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * РЕАЛИЗАЦИЯ ReaderDao ДЛЯ РАБОТЫ С БАЗОЙ ДАННЫХ MySQL
 *
 * ОСОБЕННОСТИ РЕАЛИЗАЦИИ:
 * 1. Использует Spring JDBC Framework для упрощения работы с БД
 * 2. Применяет паттерн DAO (Data Access Object) для изоляции логики доступа к данным
 * 3. Использует JdbcTemplate для выполнения SQL запросов
 * 4. Поддерживает получение сгенерированных ID при вставке новых записей
 * 5. Инкапсулирует логику преобразования ResultSet в объекты Java
 *
 * СТРУКТУРА БАЗЫ ДАННЫХ (таблица reader):
 * - id BIGINT PRIMARY KEY AUTO_INCREMENT
 * - name VARCHAR(255) UNIQUE (логин пользователя)
 * - password_hash VARCHAR(64) (SHA-256 в hex формате)
 * - created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 */
public class MysqlReaderDao implements ReaderDao {

    // JdbcOperations - интерфейс Spring для выполнения SQL операций
    // Это может быть JdbcTemplate или другой объект, реализующий интерфейс
    // Используем интерфейс, а не конкретную реализацию для слабой связанности
    private final JdbcOperations jdbc;

    // JdbcTemplate - конкретная реализация JdbcOperations
    // Нужен для методов, требующих специфичной функциональности (например, работа с KeyHolder)
    private final JdbcTemplate jdbcTemplate;

    /**
     * КОНСТРУКТОР С ВНЕДРЕНИЕМ ЗАВИСИМОСТИ (Dependency Injection)
     * Принцип: объект не создает зависимости самостоятельно, а получает их извне
     *
     * @param jdbc объект для выполнения SQL запросов, обычно JdbcTemplate
     *             переданный из Factory или конфигурационного класса
     */
    public MysqlReaderDao(JdbcOperations jdbc) {
        this.jdbc = jdbc;
        // Проверяем, является ли переданный объект экземпляром JdbcTemplate
        // Если да - используем его, если нет - создаем новый (хотя этот случай маловероятен)
        this.jdbcTemplate = (jdbc instanceof JdbcTemplate jt) ? jt : new JdbcTemplate();
        // Важно: в Factory.INSTANCE.getReaderDao() должен передаваться именно JdbcTemplate
    }

    /**
     * ЭКСТРАКТОР ДАННЫХ - преобразует ResultSet (результат SQL запроса) в List<Reader>
     *
     * Используется во всех методах поиска (findById, findByUsername, findAll)
     * Реализует интерфейс ResultSetExtractor<List<Reader>>
     *
     * ПРИНЦИП РАБОТЫ:
     * 1. Создает пустой ArrayList
     * 2. В цикле обрабатывает каждую строку ResultSet
     * 3. Для каждой строки вызывает Reader.fromResultSet(rs)
     * 4. Если результат не null, добавляет в список
     * 5. Возвращает заполненный список
     */
    private final ResultSetExtractor<List<Reader>> extractor = rs -> {
        // Создаем новый список для хранения результатов
        var list = new ArrayList<Reader>();

        // ResultSet - это таблица с результатами запроса
        // rs.next() перемещает курсор к следующей строке, возвращает false если строк больше нет
        while (rs.next()) {
            // Преобразуем текущую строку ResultSet в объект Reader
            var r = Reader.fromResultSet(rs);

            // Проверяем, что преобразование прошло успешно (объект не null)
            if (r != null) list.add(r);
        }
        return list;
    };

    /**
     * ПОИСК ЧИТАТЕЛЯ ПО ID
     *
     * SQL запрос: SELECT id, name, password_hash, created_at FROM reader WHERE id = ?
     *
     * АЛГОРИТМ:
     * 1. Формируем параметризованный SQL запрос
     * 2. Выполняем запрос через jdbc.query() с передачей ID как параметра
     * 3. Используем экстрактор для преобразования ResultSet в List<Reader>
     * 4. Если список пустой - возвращаем null, иначе первый элемент списка
     *
     * @param id идентификатор читателя (первичный ключ в БД)
     * @return объект Reader или null если не найден
     */
    @Override
    public Reader findById(Long id) {
        // SQL запрос с параметром (?) для защиты от SQL инъекций
        var sql = "SELECT id, name, password_hash, created_at FROM reader WHERE id = ?";

        // jdbc.query() выполняет SQL запрос с параметрами
        // Аргументы: SQL строка, экстрактор, параметры запроса
        var list = jdbc.query(sql, extractor, id);

        // Проверяем, есть ли результаты
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * ПОИСК ЧИТАТЕЛЯ ПО ИМЕНИ ПОЛЬЗОВАТЕЛЯ
     *
     * Используется для аутентификации (логина)
     * Поле name в БД имеет UNIQUE constraint, поэтому возвращается не более одного читателя
     *
     * SQL запрос: SELECT id, name, password_hash, created_at FROM reader WHERE name = ?
     *
     * @param username имя пользователя (логин)
     * @return объект Reader или null если не найден
     */
    @Override
    public Reader findByUsername(String username) {
        var sql = "SELECT id, name, password_hash, created_at FROM reader WHERE name = ?";
        var list = jdbc.query(sql, extractor, username);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * ПОЛУЧЕНИЕ ВСЕХ ЧИТАТЕЛЕЙ
     *
     * SQL запрос: SELECT id, name, password_hash, created_at FROM reader
     *
     * @return список всех читателей в БД, отсортированных в порядке их добавления
     *         (если не указан ORDER BY, порядок не гарантирован)
     */
    @Override
    public List<Reader> findAll() {
        var sql = "SELECT id, name, password_hash, created_at FROM reader";
        return jdbc.query(sql, extractor);
    }

    /**
     * СОХРАНЕНИЕ НОВОГО ЧИТАТЕЛЯ В БАЗЕ ДАННЫХ
     *
     * ОСОБЕННОСТИ РЕАЛИЗАЦИИ:
     * 1. Использует PreparedStatement для защиты от SQL инъекций
     * 2. Возвращает сгенерированный БД ID через KeyHolder
     * 3. Устанавливает полученный ID в объект Reader
     *
     * SQL запрос: INSERT INTO reader (name, password_hash) VALUES (?, ?)
     *
     * @param reader объект Reader с заполненными полями name и passwordHash
     *               поле id может быть null - оно будет сгенерировано БД
     * @return тот же объект Reader с установленным полем id
     */
    @Override
    public Reader save(Reader reader) {
        // SQL запрос для вставки новой записи
        // Вставляем только name и password_hash, created_at заполняется автоматически (DEFAULT CURRENT_TIMESTAMP)
        final String sql = "INSERT INTO reader (name, password_hash) VALUES (?, ?)";

        // KeyHolder - объект Spring для получения сгенерированных ключей
        // GeneratedKeyHolder - реализация для автоинкрементных полей
        KeyHolder keyHolder = new GeneratedKeyHolder();

        /**
         * ИСПОЛЬЗУЕМ jdbcTemplate.update() с Callback
         *
         * Почему jdbcTemplate, а не jdbc?
         * JdbcTemplate.update() с PreparedStatementCreator и KeyHolder позволяет
         * получить сгенерированный ID после вставки.
         *
         * Лямбда-выражение создает PreparedStatement с флагом RETURN_GENERATED_KEYS
         */
        jdbcTemplate.update(conn -> {
            // Создаем PreparedStatement с указанием, что нужно вернуть сгенерированные ключи
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Устанавливаем параметры запроса (индексы начинаются с 1)
            ps.setString(1, reader.getName());           // первый параметр - имя пользователя
            ps.setString(2, reader.getPasswordHash());   // второй параметр - хэш пароля

            return ps;
        }, keyHolder); // keyHolder получит сгенерированные ID

        // Получаем сгенерированный ID из KeyHolder
        Number key = keyHolder.getKey();

        // Проверяем, что ID был успешно сгенерирован
        if (key != null) {
            // Устанавливаем полученный ID в объект Reader
            reader.setId(key.longValue());
        }
        // Важно: если ID не сгенерирован, объект вернется без ID
        // В реальном приложении здесь может быть дополнительная обработка ошибок

        return reader;
    }
}