package booklib.readers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * РЕАЛИЗАЦИЯ ReaderDao ДЛЯ ХРАНЕНИЯ В ПАМЯТИ
 * Используется для тестирования и демо-режима.
 */
public class MemoryReaderDao implements ReaderDao {

    private final List<Reader> readers;

    public MemoryReaderDao(List<Reader> readers) {
        this.readers = readers;
    }

    @Override
    public Reader findById(Long id) {
        // Линейный поиск по ID
        for (var r : readers) {
            if (r.getId() != null && r.getId().equals(id)) return r;
        }
        return null;
    }

    @Override
    public Reader findByUsername(String username) {
        // Линейный поиск по имени пользователя
        for (var r : readers) {
            if (r.getName() != null && r.getName().equals(username)) return r;
        }
        return null;
    }

    @Override
    public List<Reader> findAll() {
        // Возвращаем копию списка для защиты от изменений
        return new ArrayList<>(readers);
    }

    @Override
    public Reader save(Reader reader) {
        // Если ID не установлен - генерируем новый
        if (reader.getId() == null) {
            // Находим максимальный ID в списке и увеличиваем на 1
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