package org.gh.afriluck.afriluckussd.utils;


import org.gh.afriluck.afriluckussd.entities.Session;
import org.gh.afriluck.afriluckussd.entities.SessionBackup;
import org.gh.afriluck.afriluckussd.mapping.SessionBackupMapper;
import org.gh.afriluck.afriluckussd.repositories.CustomerSessionRepository;
import org.gh.afriluck.afriluckussd.repositories.SessionBackupRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class SessionTaskSchedulers implements Runnable {

    private CustomerSessionRepository customerSessionRepository;
    private SessionBackupRepository sessionBackupRepository;

    Thread.Builder sessionThread = Thread.ofVirtual().name("Session Thread");

    public SessionTaskSchedulers(CustomerSessionRepository customerSessionRepository, SessionBackupRepository sessionBackupRepository) {
        this.customerSessionRepository = customerSessionRepository;
        this.sessionBackupRepository = sessionBackupRepository;
    }

    @Override
    //@Scheduled(cron = "0 0/70 * * * *", zone = "GMT")
    //@Scheduled(cron = "0 * * * * *")
    @Scheduled(fixedRate = 4200000)
    public void run() {
        try {
            System.out.println("Cleaning Session...\n");
            Date date = new Date();
            int currentHour = LocalTime.now().getHour() - 1;
            System.out.printf("Hour => %s", String.valueOf(currentHour));
            List<Session> sessions = StreamSupport.stream(customerSessionRepository.findAll().spliterator(), false).collect(Collectors.toList());
            List<SessionBackup> sessionBackups = sessions.stream().map(SessionBackupMapper::mapToEntity).collect(Collectors.toList());
            Runnable sessionTask = () -> {
                System.out.println("Backup Session Data Thread running...");
                sessionBackupRepository.saveAll(sessionBackups);
            };
            sessionThread.start(sessionTask);
            customerSessionRepository.deleteOldSessions(String.valueOf(currentHour));
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
