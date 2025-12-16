package booklib.readers;

import java.util.List;

public interface ReaderDao {
    Reader findById(Long id);
    Reader findByUsername(String username); // по полю name в БД
    List<Reader> findAll();

    Reader save(Reader reader); // insert (и вернуть Reader с id)
}
