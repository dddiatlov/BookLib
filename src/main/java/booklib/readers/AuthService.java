package booklib.readers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class AuthService {

    private final ReaderDao readerDao;

    public AuthService(ReaderDao readerDao) {
        this.readerDao = readerDao;
    }

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

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

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
