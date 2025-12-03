package booklib.reader;

import java.io.File;
import java.util.List;
import booklib.entities.Reader;

public interface ReaderDao {
    int loadFromCsv(File file);
    List<Reader> findAll();
}
