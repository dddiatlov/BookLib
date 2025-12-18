package booklib;

import booklib.readers.Reader;

/**
 * Simple in-memory session holder for the currently logged-in reader.
 * (Good enough for a student JavaFX app.)
 */
public final class Session {

    private static Reader currentReader;

    private Session() { }

    public static void setCurrentReader(Reader reader) {
        currentReader = reader;
    }

    public static Reader getCurrentReader() {
        return currentReader;
    }

    public static long requireReaderId() {
        if (currentReader == null || currentReader.getId() == null) {
            throw new IllegalStateException("No logged-in reader in Session.");
        }
        return currentReader.getId();
    }

    public static void clear() {
        currentReader = null;
    }
}
