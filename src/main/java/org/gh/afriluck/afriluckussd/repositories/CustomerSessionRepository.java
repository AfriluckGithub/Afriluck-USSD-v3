package org.gh.afriluck.afriluckussd.repositories;

import org.gh.afriluck.afriluckussd.entities.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CustomerSessionRepository extends CrudRepository<Session, Integer> {

    public Session findBySequenceID(String sessionId);

    @Query(value = "DELETE FROM session WHERE \"created_at\" < NOW()-'1 HOUR'::INTERVAL;", nativeQuery = true)
    public void deleteOldSessions();
}
