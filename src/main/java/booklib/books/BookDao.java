package booklib.books;

import java.io.File;
import java.util.List;

public interface BookDao {

    // загрузка списка книг в "store" из csv (как студентов)
    int loadFromCsv(File file);

    // все книги из глобального списка (store)
    List<Book> findAll();

    // одна книга по id
    Book findById(Long id);

    // книги, которые привязаны к конкретному reader'у (его профиль)
    List<Book> findByReaderId(Long readerId);

    // привязать книгу из store к reader'у с каким-то статусом (например "PLANNED", "READING", ...)
    void addBookForReader(Long bookId, Long readerId, String status);
}
