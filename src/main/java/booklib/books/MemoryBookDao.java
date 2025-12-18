package booklib.books;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MemoryBookDao implements BookDao {

    private final List<Book> books;

    public MemoryBookDao(List<Book> books) {
        this.books = books;
    }

    @Override
    public int loadFromCsv(File file) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // expected CSV columns:
                // id,title,author,pages,genre,language,created_at
                String[] parts = splitCsv(line);
                if (parts.length < 7) continue;

                if (parts[0].equalsIgnoreCase("id")) continue;

                Book b = new Book();
                b.setId(Long.parseLong(parts[0].trim()));
                b.setTitle(parts[1].trim());
                b.setAuthor(parts[2].trim());
                b.setPages(Integer.parseInt(parts[3].trim()));
                b.setGenre(parts[4].trim());
                b.setLanguage(parts[5].trim());
                b.setCreatedAt(LocalDateTime.parse(parts[6].trim()));

                books.add(b);
                count++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot load CSV: " + e.getMessage(), e);
        }
        return count;
    }

    @Override
    public void add(Book book) {
        books.add(book);
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books);
    }

    @Override
    public Book findById(Long id) {
        return books.stream().filter(b -> Objects.equals(b.getId(), id)).findFirst().orElse(null);
    }

    @Override
    public List<Book> findByReaderId(Long readerId) {
        // memory mode: no persistence
        return List.of();
    }

    @Override
    public void addBookForReader(Long bookId, Long readerId, String status) {
        // memory mode: no-op
    }

    @Override
    public void removeBookForReader(Long bookId, Long readerId) {
        // memory mode: no-op
    }

    @Override
    public boolean isFavorite(long readerId, long bookId) {
        // In-memory DAO does not support favorites
        return false;
    }

    @Override
    public void addFavorite(long readerId, long bookId) {
        // no-op for memory implementation
    }

    @Override
    public void removeFavorite(long readerId, long bookId) {
        // no-op for memory implementation
    }

    @Override
    public List<Book> findFavoritesByReaderId(long readerId) {
        return List.of();
    }

    private static String[] splitCsv(String line) {
        // Simple CSV splitter (no quoted commas handling). Enough for your dataset.
        return Arrays.stream(line.split(",", -1))
                .map(s -> s.replace("\uFEFF", "")) // BOM
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }
}
