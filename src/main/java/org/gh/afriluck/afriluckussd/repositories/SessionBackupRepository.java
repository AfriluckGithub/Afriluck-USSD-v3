package org.gh.afriluck.afriluckussd.repositories;

import org.gh.afriluck.afriluckussd.entities.SessionBackup;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;


public interface SessionBackupRepository extends CrudRepository<SessionBackup, UUID> {
}
