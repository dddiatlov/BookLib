package booklib.reader;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MysqlReaderDao implements ReaderDao {

    private final JdbcOperations jdbcOperations;

    private final ResultSetExtractor<List<Reader>> resultSetExtractor = rs -> {
        var readers = new ArrayList<Reader>();
        while (rs.next()) {
            var reader = Reader.fromResultSet(rs);
            readers.add(reader);
        }
        return readers;
    };

    private final String selectQuery =
            "SELECT id, name, password_hash, created_at FROM reader";

    public MysqlReaderDao(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public int loadFromCsv(File file) {
        var memoryDao = new MemoryReaderDao(new ArrayList<>());

        var numLoaded = memoryDao.loadFromCsv(file);
        var readers = memoryDao.findAll();

        readers.forEach(reader -> jdbcOperations.update(
                "INSERT INTO reader (id, name, password_hash, created_at) VALUES (?, ?, ?, ?)",
                reader.getId(),
                reader.getName(),
                reader.getPasswordHash(),
                reader.getCreatedAt()
        ));

        return numLoaded;
    }

    @Override
    public List<Reader> findAll() {
        return jdbcOperations.query(selectQuery, resultSetExtractor);
    }
}
