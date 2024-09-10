package com.maureva.demosession.controller;


import com.maureva.demosession.dto.SessionDto;
import com.maureva.demosession.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sessions")
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/active")
    public ResponseEntity<List<SessionDto>> findAllSessions() {
        List<SessionDto> activeUsers = sessionService.findAllActiveSessions();
        return new ResponseEntity<>(activeUsers, HttpStatus.OK);
    }

    @GetMapping("expire-username/{username}")
    public ResponseEntity<Void> expireByUsername(@PathVariable("username") String username) {
        sessionService.expireUserSessionsByusername(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("expire-sessionId/{sessionId}")
    public ResponseEntity<String> expire(@PathVariable("sessionId") String sessionId) {
        String username = sessionService.expireUserSession(sessionId);
        return ResponseEntity.ok("Session killed for username: " + username);
    }


}
