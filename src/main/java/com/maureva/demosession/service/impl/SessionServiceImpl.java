package com.maureva.demosession.service.impl;

import com.maureva.demosession.dto.SessionDto;
import com.maureva.demosession.service.SessionService;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class SessionServiceImpl implements SessionService {
    private static final String REMOTE_ADDRESS = "REMOTE_ADDRESS";
    private final SessionRegistry sessionRegistry;
    private final SessionRepository<? extends Session> sessionRepository;

    public SessionServiceImpl(SessionRegistry sessionRegistry,
                              SessionRepository<? extends Session> sessionRepository) {
        this.sessionRegistry = sessionRegistry;
        this.sessionRepository = sessionRepository;
    }


    @Override
    public List<SessionDto> findAllActiveSessions() {

        List<SessionDto> result = new ArrayList<>();
        sessionRegistry.getAllPrincipals()
                .forEach(principal -> sessionRegistry.getAllSessions(principal, false).forEach(
                        sessionInfo -> {
                            Session session = sessionRepository.findById(sessionInfo.getSessionId());
                            SessionDto sessionDto = new SessionDto();
                            if (Objects.nonNull(session)) {
                                sessionDto.setRemoteAddress(session.getAttribute(REMOTE_ADDRESS));
                                sessionDto.setCreationDate(format(session.getCreationTime()));
                            }
                            sessionDto.setSessionId(sessionInfo.getSessionId());
                            sessionDto.setUsername(((UserDetails) sessionInfo.getPrincipal()).getUsername());
                            result.add(sessionDto);
                        }
                ));
        return result;
    }

    private LocalDateTime format(Instant instant) {
        Timestamp ts = Timestamp.from(instant);
        return ts.toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public void expireUserSessionsByusername(String username) {

        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof UserDetails userDetails &&
                (userDetails.getUsername().equalsIgnoreCase(username))) {
                for (SessionInformation information : sessionRegistry.getAllSessions(userDetails, true)) {
                    information.expireNow();
                }
                break;

            }
        }
    }

    @Override
    public String expireUserSession(String sessionId) {
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
        String username = ((UserDetails) sessionInformation.getPrincipal()).getUsername();

        sessionInformation.expireNow();
        sessionRepository.deleteById(sessionId);
        return username;
    }

}
