package booklib.readingSessions;

import java.util.List;

public interface ReadingSessionDao {
    List<ReadingSession> findAll();
    List<ReadingSession> findAllSortedByDate();

    List<ReadingSession> findByReaderIdAndBookId(long readerId, long bookId);

    ReadingSession findById(Long id);

    ReadingSession create(ReadingSession session);
    ReadingSession update(ReadingSession session);

    void delete(Long id);
}
