package booklib.readingSessions;

import java.util.List;

public interface ReadingSessionDao {
    List<ReadingSession> findAll();
    List<ReadingSession> findAllSortedByDate();
    ReadingSession create(ReadingSession session);
    ReadingSession update(ReadingSession session);
    void delete(Long id);
}
