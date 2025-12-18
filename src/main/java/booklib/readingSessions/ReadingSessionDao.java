package booklib.readingSessions;

import java.util.List;

/**
 * ИНТЕРФЕЙС ДОСТУПА К ДАННЫМ СЕССИЙ ЧТЕНИЯ (DAO)
 * Определяет контракт для работы с сессиями чтения.
 */
public interface ReadingSessionDao {
    /**
     * Получить все сессии чтения
     */
    List<ReadingSession> findAll();

    /**
     * Получить все сессии чтения, отсортированные по дате (сначала новые)
     */
    List<ReadingSession> findAllSortedByDate();

    /**
     * Найти все сессии чтения для определенного читателя и книги
     */
    List<ReadingSession> findByReaderIdAndBookId(long readerId, long bookId);

    /**
     * Найти сессию чтения по ID
     */
    ReadingSession findById(Long id);

    /**
     * Создать новую сессию чтения
     */
    ReadingSession create(ReadingSession session);

    /**
     * Обновить существующую сессию чтения
     */
    ReadingSession update(ReadingSession session);

    /**
     * Удалить сессию чтения по ID
     */
    void delete(Long id);

    /**
     * Получить сумму прочитанных страниц для определенного читателя и книги
     * Используется для контроля, чтобы не превысить общее количество страниц в книге
     */
    int sumPagesForReaderAndBook(long readerId, long bookId);
}