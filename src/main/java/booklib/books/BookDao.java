package booklib.books;

import java.io.File;
import java.util.List;

/**
 * ИНТЕРФЕЙС ДОСТУПА К ДАННЫМ КНИГ (DAO - Data Access Object)
 * Определяет контракт для работы с книгами, независимо от реализации (память/БД).
 * Паттерн DAO позволяет изолировать бизнес-логику от способа хранения данных.
 */
public interface BookDao {

    /**
     * Загружает книги из CSV файла в хранилище
     * @param file CSV файл с книгами
     * @return количество загруженных книг
     */
    int loadFromCsv(File file);

    /**
     * Добавляет новую книгу
     */
    void add(Book book);

    /**
     * Возвращает все книги
     */
    List<Book> findAll();

    /**
     * Находит книгу по ID
     */
    Book findById(Long id);

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С ПОЛЬЗОВАТЕЛЬСКИМИ КНИГАМИ ==========

    /**
     * Находит все книги конкретного читателя (из таблицы book_status)
     */
    List<Book> findByReaderId(Long readerId);

    /**
     * Добавляет связь между книгой и читателем (добавляет в "Мои книги")
     * @param status статус чтения (например, "WANT_TO_READ")
     */
    void addBookForReader(Long bookId, Long readerId, String status);

    /**
     * Удаляет книгу из "Моих книг" читателя
     */
    void removeBookForReader(Long bookId, Long readerId);

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С ИЗБРАННЫМИ ==========

    /**
     * Проверяет, находится ли книга в избранном у читателя
     */
    boolean isFavorite(long readerId, long bookId);

    /**
     * Добавляет книгу в избранное
     */
    void addFavorite(long readerId, long bookId);

    /**
     * Удаляет книгу из избранного
     */
    void removeFavorite(long readerId, long bookId);

    /**
     * Находит все избранные книги читателя
     */
    List<Book> findFavoritesByReaderId(long readerId);
}