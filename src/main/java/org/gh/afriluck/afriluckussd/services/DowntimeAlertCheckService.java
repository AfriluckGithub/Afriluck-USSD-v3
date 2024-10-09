package org.gh.afriluck.afriluckussd.services;

import org.gh.afriluck.afriluckussd.repositories.CustomerSessionRepository;
import org.gh.afriluck.afriluckussd.utils.AfriluckCallHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;

public class DowntimeAlertCheckService {

    @Autowired
    CustomerSessionRepository customerSessionRepository;
    private AfriluckCallHandler handler;

    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkForNullCustomerIds() {
        long nullCount = customerSessionRepository.countNullCustomerIds();

        if (nullCount > 5) {
            System.out.println("More than 5 NULL customer_id entries in the last minute! Count: " + nullCount);
            String body = String.format("{\"phone_number\":\"%s\",\"message\":\"%s\"}", "233594951335", "Possible USSD downtime alert");
            ResponseEntity<String> response = handler.client()
                    .post()
                    .uri("/api/V1/send-sms")
                    .body(body)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);
        }
    }
}
