package booklib.readers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MemoryReaderDao implements ReaderDao {

    private final List<Reader> readers;

    public MemoryReaderDao(List<Reader> readers) {
        this.readers = readers;
    }

    @Override
    public Reader findById(Long id) {
        for (var r : readers) {
            if (r.getId() != null && r.getId().equals(id)) return r;
        }
        return null;
    }

    @Override
    public Reader findByUsername(String username) {
        for (var r : readers) {
            if (r.getName() != null && r.getName().equals(username)) return r;
        }
        return null;
    }

    @Override
    public List<Reader> findAll() {
        return new ArrayList<>(readers);
    }

    @Override
    public Reader save(Reader reader) {
        if (reader.getId() == null) {
            long nextId = readers.stream()
                    .map(Reader::getId)
                    .filter(x -> x != null)
                    .max(Comparator.naturalOrder())
                    .orElse(0L) + 1;
            reader.setId(nextId);
        }
        readers.add(reader);
        return reader;
    }
}
