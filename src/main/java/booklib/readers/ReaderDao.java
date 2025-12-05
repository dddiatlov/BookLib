package booklib.readers;

import java.io.File;
import java.util.List;

public interface ReaderDao {
    int loadFromCsv(File file);
    List<Reader> findAll();
}
