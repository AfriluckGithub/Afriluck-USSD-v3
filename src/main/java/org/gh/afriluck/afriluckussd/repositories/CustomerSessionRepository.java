package org.gh.afriluck.afriluckussd.repositories;

import org.gh.afriluck.afriluckussd.entities.Session;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface CustomerSessionRepository extends CrudRepository<Session, Integer> {

    public Session findBySequenceID(String sessionId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Session s WHERE EXTRACT(HOUR FROM s.createdDate) >= 0 AND EXTRACT(HOUR FROM s.createdDate) <= :hour AND DATE(s.createdDate) = DATE(NOW())")
    public void deleteOldSessions(@Param("hour") String hour);
}
