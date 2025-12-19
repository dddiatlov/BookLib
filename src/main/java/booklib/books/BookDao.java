package booklib.books;

import java.io.File;
import java.util.List;

public interface BookDao {

    int loadFromCsv(File file);

    void add(Book book);

    List<Book> findAll();

    Book findById(Long id);

    List<Book> findByReaderId(Long readerId);

    void addBookForReader(Long bookId, Long readerId, String status);

    void removeBookForReader(Long bookId, Long readerId);

    boolean isFavorite(long readerId, long bookId);

    void addFavorite(long readerId, long bookId);

    void removeFavorite(long readerId, long bookId);

    List<Book> findFavoritesByReaderId(long readerId);
}
