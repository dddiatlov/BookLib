package booklib.readers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * СЕРВИС АУТЕНТИФИКАЦИИ
 * Отвечает за логин и регистрацию пользователей.
 * Использует хэширование паролей (SHA-256) для безопасности.
 */
public class AuthService {

    private final ReaderDao readerDao;

    public AuthService(ReaderDao readerDao) {
        this.readerDao = readerDao;
    }

    /**
     * АВТОРИЗАЦИЯ ПОЛЬЗОВАТЕЛЯ
     * 1. Проверяет наличие пользователя с таким именем
     * 2. Сравнивает хэш пароля
     * @return объект Reader при успешной авторизации
     * @throws IllegalArgumentException при ошибках валидации
     */
    public Reader login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalArgumentException("Username/password cannot be empty.");
        }

        Reader r = readerDao.findByUsername(username.trim());
        if (r == null) {
            throw new IllegalArgumentException("User not found.");
        }

        String hash = sha256(password);
        if (!hash.equals(r.getPasswordHash())) {
            throw new IllegalArgumentException("Wrong password.");
        }

        return r;
    }

    /**
     * РЕГИСТРАЦИЯ НОВОГО ПОЛЬЗОВАТЕЛЯ
     * 1. Проверяет уникальность имени пользователя
     * 2. Хэширует пароль
     * 3. Сохраняет пользователя в БД
     */
    public Reader register(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalArgumentException("Username/password cannot be empty.");
        }

        username = username.trim();

        if (readerDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("User already exists.");
        }

        Reader r = new Reader();
        r.setName(username);
        r.setPasswordHash(sha256(password));
        return readerDao.save(r);
    }

    /**
     * Проверка строки на пустоту или null
     */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * ХЭШИРОВАНИЕ ПАРОЛЯ С ПОМОЩЬЮ SHA-256
     * Преобразует пароль в hex-строку для безопасного хранения
     */
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}