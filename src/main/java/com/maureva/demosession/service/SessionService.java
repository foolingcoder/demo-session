package com.maureva.demosession.service;

import com.maureva.demosession.dto.SessionDto;

import java.util.List;

public interface SessionService {
    List<SessionDto> findAllActiveSessions();

    void expireUserSessionsByusername(String username);

    String expireUserSession(String sessionId);
}
