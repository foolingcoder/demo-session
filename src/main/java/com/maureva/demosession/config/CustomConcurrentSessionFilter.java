package com.maureva.demosession.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomConcurrentSessionFilter extends ConcurrentSessionFilter {

    private static final String AUTHENTICATION_ERROR_MAXIMUM_SESSION_KEY = "authentication.error.maxSession";
    private final SessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;
    private final List<LogoutHandler> handlers = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        handlers.add(new SecurityContextLogoutHandler());
    }

    public CustomConcurrentSessionFilter(SessionRegistry sessionRegistry, ObjectMapper objectMapper) {
        super(sessionRegistry);
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        if (session != null) {
            SessionInformation info = this.sessionRegistry.getSessionInformation(session.getId());

            if (info != null && info.isExpired()) {

                executeLogout(request, response);

                response.getOutputStream().println(objectMapper.writeValueAsString(AUTHENTICATION_ERROR_MAXIMUM_SESSION_KEY));
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }
        chain.doFilter(request, response);
    }


    private void executeLogout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        for (LogoutHandler handler : handlers) {
            handler.logout(request, response, auth);
        }
    }

    public void addLogoutHandler(LogoutHandler logoutHandler) {
        Assert.notNull(logoutHandler, "LogoutHandler required");
        handlers.add(logoutHandler);
    }

}