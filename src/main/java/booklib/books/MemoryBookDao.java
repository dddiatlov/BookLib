package booklib.books;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * РЕАЛИЗАЦИЯ BookDao ДЛЯ ХРАНЕНИЯ ДАННЫХ В ПАМЯТИ
 * Используется:
 * 1. Для тестирования без базы данных
 * 2. Как временное хранилище при загрузке CSV
 * 3. В демо-режиме приложения
 *
 * ОСОБЕННОСТИ:
 * - Данные теряются после закрытия приложения
 * - Не поддерживает связи между читателями и книгами
 * - Не поддерживает избранные книги
 * - Простая реализация для быстрого старта
 */
public class MemoryBookDao implements BookDao {

    // Хранилище - обычный ArrayList в памяти
    private final List<Book> books;

    /**
     * Конструктор принимает существующий список
     * Это позволяет инициализировать DAO предзагруженными данными
     */
    public MemoryBookDao(List<Book> books) {
        this.books = books;
    }

    /**
     * ЗАГРУЗКА ИЗ CSV
     * Формат файла: id,title,author,pages,genre,language,created_at
     * Особенности:
     * - Пропускает пустые строки
     * - Пропускает заголовок (строку с "id")
     * - Бросает RuntimeException при ошибке (т.к. в памяти нет транзакций)
     */
    @Override
    public int loadFromCsv(File file) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

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

    /**
     * ПРОСТЫЕ ОПЕРАЦИИ (работают полностью)
     */
    @Override
    public void add(Book book) {
        books.add(book);
    }

    @Override
    public List<Book> findAll() {
        // Возвращаем копию, чтобы внешний код не мог изменить внутренний список
        return new ArrayList<>(books);
    }

    @Override
    public Book findById(Long id) {
        // Используем Stream API для поиска
        return books.stream()
                .filter(b -> Objects.equals(b.getId(), id))
                .findFirst()
                .orElse(null);
    }

    // ========== ЗАГЛУШКИ ==========
    /**
     * СЛЕДУЮЩИЕ МЕТОДЫ - ЗАГЛУШКИ
     * В памяти не реализованы связи между сущностями
     * Эти методы работают только в MysqlBookDao
     */

    @Override
    public List<Book> findByReaderId(Long readerId) {
        return List.of(); // Всегда пустой список
    }

    @Override
    public void addBookForReader(Long bookId, Long readerId, String status) {
        // Ничего не делаем - в памяти нет связи читатель-книга
    }

    @Override
    public void removeBookForReader(Long bookId, Long readerId) {
        // Ничего не делаем
    }

    @Override
    public boolean isFavorite(long readerId, long bookId) {
        return false; // В памяти нет избранного
    }

    @Override
    public void addFavorite(long readerId, long bookId) {
        // Ничего не делаем
    }

    @Override
    public void removeFavorite(long readerId, long bookId) {
        // Ничего не делаем
    }

    @Override
    public List<Book> findFavoritesByReaderId(long readerId) {
        return List.of(); // Пустой список
    }

    /**
     * ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ПАРСИНГА CSV
     * Упрощенный парсер (не обрабатывает кавычки и экранированные запятые)
     * Удаляет BOM (Byte Order Mark) - невидимый символ в начале некоторых UTF-8 файлов
     */
    private static String[] splitCsv(String line) {
        return Arrays.stream(line.split(",", -1))
                .map(s -> s.replace("\uFEFF", "")) // Удаление BOM
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }
}