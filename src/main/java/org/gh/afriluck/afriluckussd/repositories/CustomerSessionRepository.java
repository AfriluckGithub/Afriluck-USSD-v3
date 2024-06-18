package org.gh.afriluck.afriluckussd.repositories;

import org.gh.afriluck.afriluckussd.entities.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSessionRepository extends CrudRepository<Session, Integer> {
    public Session findBySequenceID(String sessionId);
}
