package booklib.books;

import java.io.File;
import java.util.List;

public interface BookDao {
    int loadFromCsv(File file);

    void add(Book book);

    List<Book> findAll();

    Book findById(Long id);

    // My Books persistence (book_status)
    List<Book> findByReaderId(Long readerId);

    void addBookForReader(Long bookId, Long readerId, String status);

    void removeBookForReader(Long bookId, Long readerId);
}
