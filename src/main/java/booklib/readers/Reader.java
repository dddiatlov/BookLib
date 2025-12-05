package booklib.readers;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Data
public class Reader {
    private Long id;
    private String name;
    private String passwordHash;
    private LocalDateTime createdAt;

    public static Reader fromResultSet(ResultSet rs) throws SQLException {
        return fromResultSet(rs, "");
    }

    public static Reader fromResultSet(ResultSet rs, String aliasPrefix) throws SQLException {
        Long id = rs.getLong(aliasPrefix + "id");
        if (rs.wasNull()) {
            return null;
        }

        var reader = new Reader();
        reader.setId(id);
        reader.setName(rs.getString(aliasPrefix + "name"));
        reader.setPasswordHash(rs.getString(aliasPrefix + "password_hash"));

        var ts = rs.getTimestamp(aliasPrefix + "created_at");
        reader.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

        return reader;
    }
}
