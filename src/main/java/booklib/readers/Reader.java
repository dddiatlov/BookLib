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
        long idVal = rs.getLong(aliasPrefix + "id");
        if (rs.wasNull()) return null;

        Reader r = new Reader();
        r.setId(idVal);
        r.setName(rs.getString(aliasPrefix + "name"));
        r.setPasswordHash(rs.getString(aliasPrefix + "password_hash"));

        var ts = rs.getTimestamp(aliasPrefix + "created_at");
        r.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

        return r;
    }
}
