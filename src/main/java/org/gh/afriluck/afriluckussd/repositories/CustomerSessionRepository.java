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
}
