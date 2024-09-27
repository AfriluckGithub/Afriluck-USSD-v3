package org.gh.afriluck.afriluckussd.controllers;

import org.gh.afriluck.afriluckussd.dto.Event;
import org.gh.afriluck.afriluckussd.entities.Session;
import org.gh.afriluck.afriluckussd.repositories.CustomerSessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
public class SessionController {

    private CustomerSessionRepository sessionRepository;

    public SessionController(CustomerSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @GetMapping(path = "/sessions")
    public ResponseEntity<List<Session>> getSessionRecords() {
        try {
            List<Session> sessions = sessionRepository.getSessionsOrderedByCreatedDateDesc();
            List<Session> streamed = sessions.stream().filter(session ->
                            session.getData() != null
                                    && session.getData().toLowerCase().contains("unknown")
                                    || session.getPosition() > 9
                                    || session.getPosition().equals(1) && Integer.parseInt(session.getData()) > 6)
                    .collect(Collectors.toList());
            //List<Session> sessions = StreamSupport.stream(iterableSessions.spliterator(), false).collect(Collectors.toList());
            return ResponseEntity.ok().body(streamed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @GetMapping(path = "/events")
    public ResponseEntity<List> getEventCounts() {
        try {
            List<Object[]> events = sessionRepository.getSessionsOrderedByCreatedDateDescGroupBySessionCount();

            List<Map<String, String>> structuredSessions = events.stream()
                    .map(event -> {
                        Map<String, String> sessionMap = new HashMap<>();
                        sessionMap.put("hour", event[0].toString());
                        sessionMap.put("event", event[1].toString());
                        return sessionMap;
                    }).toList();
            return ResponseEntity.ok().body(structuredSessions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping(path = "/networks")
    public ResponseEntity<List> getSessionByNetworkCount() {
        try {
            List<Object[]> events = sessionRepository.getSessionByNetworkCount();

            List<Map<String, String>> structuredSessions = events.stream()
                    .map(event -> {
                        Map<String, String> sessionMap = new HashMap<>();
                        sessionMap.put("event", event[0].toString());
                        sessionMap.put("network", event[1].toString());
                        return sessionMap;
                    }).toList();
            return ResponseEntity.ok().body(structuredSessions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
