package com.maureva.demosession.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.LogoutHandler;


public class CustomLogoutHandler implements LogoutHandler {

    private final SessionRegistry sessionRegistry;


    public CustomLogoutHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            sessionRegistry.getSessionInformation(session.getId()).expireNow();
            sessionRegistry.removeSessionInformation(session.getId());
        }
    }

}
