package org.gh.afriluck.afriluckussd.mapping;

import org.gh.afriluck.afriluckussd.entities.Session;
import org.gh.afriluck.afriluckussd.entities.SessionBackup;

import java.time.LocalDateTime;

public class SessionBackupMapper {


    public static SessionBackup mapToEntity(Session session) {
        SessionBackup sessionBackup = new SessionBackup();
        sessionBackup.setAmount(session.getAmount());
        sessionBackup.setMsisdn(session.getMsisdn());
        sessionBackup.setNetwork(session.getNetwork());
        sessionBackup.setGameTypeId(session.getGameTypeId());
        sessionBackup.setTimeStamp(LocalDateTime.now().toString());
        return sessionBackup;
    }
}
