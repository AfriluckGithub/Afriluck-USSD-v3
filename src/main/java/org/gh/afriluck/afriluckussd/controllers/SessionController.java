package org.gh.afriluck.afriluckussd.controllers;

import org.gh.afriluck.afriluckussd.entities.Session;
import org.gh.afriluck.afriluckussd.repositories.CustomerSessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/analytics")
public class SessionController {

    private CustomerSessionRepository sessionRepository;

    public SessionController(CustomerSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @GetMapping(path = "/sessions")
    public ResponseEntity<List<Session>> getSessionRecords() {
        Iterable<Session> iterableSessions = sessionRepository.findAll();
        List<Session> sessions = StreamSupport.stream(iterableSessions.spliterator(), false).collect(Collectors.toList());
        return ResponseEntity.ok().body(sessions);
    }
}
