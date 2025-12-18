package booklib.readers;

import java.util.List;

/**
 * ИНТЕРФЕЙС ДОСТУПА К ДАННЫМ ЧИТАТЕЛЕЙ (DAO)
 * Определяет контракт для работы с читателями.
 * Использует паттерн DAO для абстракции доступа к данным.
 */
public interface ReaderDao {
    /**
     * Находит читателя по ID
     */
    Reader findById(Long id);

    /**
     * Находит читателя по имени пользователя (поле 'name' в БД)
     * Используется для аутентификации
     */
    Reader findByUsername(String username);

    /**
     * Возвращает всех читателей
     */
    List<Reader> findAll();

    /**
     * Сохраняет читателя в хранилище
     * @return Reader с установленным ID (после генерации в БД)
     */
    Reader save(Reader reader);
}