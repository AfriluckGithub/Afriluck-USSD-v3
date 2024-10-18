package org.gh.afriluck.afriluckussd.repositories;

import org.gh.afriluck.afriluckussd.entities.SessionRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SessionRequestRepository extends CrudRepository<SessionRequest, UUID> {
}
