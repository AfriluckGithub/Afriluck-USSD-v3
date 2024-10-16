package org.gh.afriluck.afriluckussd.repositories;

import org.gh.afriluck.afriluckussd.dto.Event;
import org.gh.afriluck.afriluckussd.entities.Session;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerSessionRepository extends CrudRepository<Session, UUID> {

    public Session findBySequenceID(String sessionId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Session s WHERE EXTRACT(HOUR FROM s.createdDate) >= 0 AND EXTRACT(HOUR FROM s.createdDate) <= :hour AND DATE(s.createdDate) = DATE(NOW())")
    void deleteOldSessions(@Param("hour") String hour);

    @Query("SELECT s FROM Session s ORDER BY s.createdDate DESC")
    List<Session> getSessionsOrderedByCreatedDateDesc();

    @Query("SELECT COUNT(s) AS event, EXTRACT(HOUR FROM s.createdDate) AS hour " +
            "FROM Session s " +
            "WHERE DATE(s.createdDate)=DATE(NOW())" +
            "GROUP BY hour " +
            "ORDER BY hour")
    List getSessionsOrderedByCreatedDateDescGroupBySessionCount();


    @Query("SELECT COUNT(s) AS event, s.network AS network " +
            "FROM Session s " +
            "WHERE DATE(s.createdDate)=DATE(NOW())" +
            "GROUP BY network " +
            "ORDER BY network")
    List getSessionByNetworkCount();

    @Query(value = "SELECT COUNT(s) FROM Session s WHERE s.data IS NULL AND s.createdDate >= CURRENT_TIMESTAMP - INTERVAL '1 MINUTE'", nativeQuery = true)
    long countNullCustomerIds();

    @Query(value = "SELECT COUNT(s) FROM Session s WHERE s.data='*741#' OR s.data='*741' AND s.createdDate >= CURRENT_TIMESTAMP - INTERVAL '1 MINUTE'", nativeQuery = true)
    long countCustomerData();
}
