package booklib;

import booklib.readers.Reader;
import lombok.Getter;
import lombok.Setter;

public final class Session {

    @Getter
    @Setter
    private static Reader currentReader;

    private Session() { ; }

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
