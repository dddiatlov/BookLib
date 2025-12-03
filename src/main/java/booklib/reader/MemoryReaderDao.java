package booklib.reader;

import booklib.entities.Reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MemoryReaderDao implements ReaderDao {

    private final List<Reader> readers;

    public MemoryReaderDao(List<Reader> readers) {
        this.readers = readers;
    }

    @Override
    public int loadFromCsv(File file) {
        var existingIds = readers.stream()
                .map(Reader::getId)
                .collect(Collectors.toSet());

        var loadedList = new ArrayList<Reader>();

        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        scanner.nextLine(); // skip header

        while (scanner.hasNextLine()) {
            var line = scanner.nextLine();
            if (line.isBlank()) continue;

            var parts = line.split(",", -1);

            var id = Long.parseLong(parts[0]);
            if (existingIds.contains(id)) continue;

            var reader = new Reader();
            reader.setId(id);
            reader.setName(parts[1]);
            reader.setPasswordHash(parts[2]);
            reader.setCreatedAt(LocalDateTime.parse(parts[3]));

            loadedList.add(reader);
        }

        readers.addAll(loadedList);
        return loadedList.size();
    }

    @Override
    public List<Reader> findAll() {
        return readers;
    }
}
