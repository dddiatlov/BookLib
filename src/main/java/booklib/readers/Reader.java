package booklib.readers;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * МОДЕЛЬ ЧИТАТЕЛЯ (Reader)
 * Соответствует таблице 'reader' в базе данных.
 * Представляет пользователя приложения.
 */
@Data
public class Reader {
    private Long id;                // Уникальный идентификатор читателя
    private String name;            // Имя пользователя (логин)
    private String passwordHash;    // Хэш пароля (SHA-256)
    private LocalDateTime createdAt; // Дата регистрации

    /**
     * Создает объект Reader из ResultSet (стандартные имена колонок)
     */
    public static Reader fromResultSet(ResultSet rs) throws SQLException {
        return fromResultSet(rs, "");
    }

    /**
     * Создает объект Reader из ResultSet с поддержкой алиасов
     * @param aliasPrefix префикс для колонок (например "r." для "r.id", "r.name")
     */
    public static Reader fromResultSet(ResultSet rs, String aliasPrefix) throws SQLException {
        long idVal = rs.getLong(aliasPrefix + "id");
        if (rs.wasNull()) return null; // Если ID null, возвращаем null

        Reader r = new Reader();
        r.setId(idVal);
        r.setName(rs.getString(aliasPrefix + "name"));
        r.setPasswordHash(rs.getString(aliasPrefix + "password_hash"));

        var ts = rs.getTimestamp(aliasPrefix + "created_at");
        r.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

        return r;
    }
}
