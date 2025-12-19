package booklib.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

public final class TestDb {

    private TestDb() {
    }

    // ⚠️ ВАЖНО: это твой порт из Factory (меняй если у тебя другой)
    private static final String URL =
            "jdbc:mysql://localhost:3307/bookLib?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    private static final String USER = "bookLib";
    private static final String PASS = "bookLib"; // если у тебя другое — поменяй

    public static JdbcTemplate jdbc() {
        return new JdbcTemplate(dataSource());
    }

    private static DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(URL);
        ds.setUsername(USER);
        ds.setPassword(PASS);
        return ds;
    }

    public static void runSchema(JdbcTemplate jdbc) {
        try (var is = TestDb.class.getClassLoader().getResourceAsStream("schema-test.sql")) {
            if (is == null) throw new IllegalStateException("schema-test.sql not found");
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            for (String stmt : sql.split(";")) {
                String s = stmt.trim();
                if (!s.isEmpty()) jdbc.execute(s);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
