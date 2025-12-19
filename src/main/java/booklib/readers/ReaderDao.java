package booklib.readers;

import java.util.List;

public interface ReaderDao {

    Reader findById(Long id);

    Reader findByUsername(String username);

    List<Reader> findAll();

    Reader save(Reader reader);
}
