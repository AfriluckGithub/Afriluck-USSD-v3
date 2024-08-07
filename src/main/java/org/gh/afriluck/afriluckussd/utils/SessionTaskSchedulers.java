package org.gh.afriluck.afriluckussd.utils;


import org.gh.afriluck.afriluckussd.repositories.CustomerSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Date;

@Component
public class SessionTaskSchedulers implements Runnable {

    private CustomerSessionRepository customerSessionRepository;

    public SessionTaskSchedulers(CustomerSessionRepository customerSessionRepository) {
        this.customerSessionRepository = customerSessionRepository;
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
            customerSessionRepository.deleteOldSessions(String.valueOf(currentHour));
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
