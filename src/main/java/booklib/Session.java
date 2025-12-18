package booklib;

import booklib.readers.Reader;

/**
 * ПРОСТОЙ КЛАСС ДЛЯ ХРАНЕНИЯ СЕССИИ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ В ПАМЯТИ
 *
 * НАЗНАЧЕНИЕ:
 * 1. Хранит данные авторизованного пользователя во время работы приложения
 * 2. Обеспечивает легкий доступ к ID текущего пользователя из любой части приложения
 * 3. Упрощает проверку авторизации
 *
 * ОГРАНИЧЕНИЯ (для студенческого проекта):
 * - Данные хранятся в оперативной памяти (исчезают после закрытия приложения)
 * - Нет поддержки нескольких одновременных сессий
 * - Нет сохранения сессии между запусками приложения
 */
public final class Session {

    // Статическое поле для хранения текущего пользователя
    private static Reader currentReader;

    // Приватный конструктор - нельзя создать экземпляр класса
    private Session() { }

    /**
     * УСТАНОВИТЬ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ
     * Вызывается после успешного входа в систему
     */
    public static void setCurrentReader(Reader reader) {
        currentReader = reader;
    }

    /**
     * ПОЛУЧИТЬ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ
     * @return объект Reader или null если пользователь не авторизован
     */
    public static Reader getCurrentReader() {
        return currentReader;
    }

    /**
     * ПОЛУЧИТЬ ID ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ С ПРОВЕРКОЙ
     *
     * КРИТИЧЕСКИ ВАЖНЫЙ МЕТОД:
     * - Используется в DAO для привязки операций к конкретному пользователю
     * - Гарантирует, что операции выполняются только для авторизованного пользователя
     *
     * @return ID пользователя (long)
     * @throws IllegalStateException если пользователь не авторизован или ID не установлен
     */
    public static long requireReaderId() {
        if (currentReader == null || currentReader.getId() == null) {
            throw new IllegalStateException("No logged-in reader in Session.");
        }
        return currentReader.getId();
    }

    /**
     * ОЧИСТИТЬ СЕССИЮ
     * Вызывается при выходе из системы
     */
    public static void clear() {
        currentReader = null;
    }
}