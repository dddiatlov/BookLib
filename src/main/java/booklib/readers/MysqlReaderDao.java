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

public class MysqlReaderDao implements ReaderDao {

    private final JdbcOperations jdbc;
    private final JdbcTemplate jdbcTemplate;

    public MysqlReaderDao(JdbcOperations jdbc) {
        this.jdbc = jdbc;
        this.jdbcTemplate = (jdbc instanceof JdbcTemplate jt) ? jt : new JdbcTemplate();
    }

    private final ResultSetExtractor<List<Reader>> extractor = rs -> {
        var list = new ArrayList<Reader>();

        while (rs.next()) {
            var r = Reader.fromResultSet(rs);
            if (r != null) list.add(r);
        }
        return list;
    };

    @Override
    public Reader findById(Long id) {
        var sql = "SELECT id, name, password_hash, created_at FROM reader WHERE id = ?";
        var list = jdbc.query(sql, extractor, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Reader findByUsername(String username) {
        var sql = "SELECT id, name, password_hash, created_at FROM reader WHERE name = ?";
        var list = jdbc.query(sql, extractor, username);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Reader> findAll() {
        var sql = "SELECT id, name, password_hash, created_at FROM reader";
        return jdbc.query(sql, extractor);
    }

    @Override
    public Reader save(Reader reader) {
        final String sql = "INSERT INTO reader (name, password_hash) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, reader.getName());
            ps.setString(2, reader.getPasswordHash());

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key != null) {
            reader.setId(key.longValue());
        }
        return reader;
    }
}
