package booklib.book;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MemoryBookDao implements BookDao {

    // глобальный список книг (store)
    private final List<Book> books;

    // ключ = readerId, значение = набор id книг в его профиле
    private final Map<Long, Set<Long>> readerBooks = new HashMap<>();

    public MemoryBookDao(List<Book> books) {
        this.books = books;
    }

    @Override
    public int loadFromCsv(File file) {
        var existingIds = books.stream()
                .map(Book::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var loaded = new ArrayList<Book>();

        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // пропускаем заголовок
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (scanner.hasNextLine()) {
            var line = scanner.nextLine();
            if (line.isBlank()) {
                continue;
            }

            // предполагаемый формат csv:
            // id,title,pages,genre,language,created_at
            var parts = line.split(",", -1);

            var id = Long.parseLong(parts[0]);
            if (existingIds.contains(id)) {
                continue;
            }

            var book = new Book();
            book.setId(id);
            book.setTitle(parts[1]);

            if (!parts[2].isBlank()) {
                book.setPages(Integer.parseInt(parts[2]));
            }

            book.setGenre(parts[3]);
            book.setLanguage(parts[4]);

            if (parts.length > 5 && !parts[5].isBlank()) {
                book.setCreatedAt(LocalDateTime.parse(parts[5]));
            }

            loaded.add(book);
        }

        books.addAll(loaded);
        return loaded.size();
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books);
    }

    @Override
    public Book findById(Long id) {
        for (var book : books) {
            if (Objects.equals(book.getId(), id)) {
                return book;
            }
        }
        return null;
    }

    @Override
    public List<Book> findByReaderId(Long readerId) {
        var ids = readerBooks.getOrDefault(readerId, Collections.emptySet());
        var result = new ArrayList<Book>();

        for (var book : books) {
            if (book.getId() != null && ids.contains(book.getId())) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public void addBookForReader(Long bookId, Long readerId, String status) {
        // статус в in-memory не используем, просто линк
        readerBooks
                .computeIfAbsent(readerId, id -> new HashSet<>())
                .add(bookId);
    }
}
