package com.maureva.demosession.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maureva.demosession.dto.response.AuthenticationResponse;
import com.maureva.demosession.service.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REMOTE_ADDRESS = "REMOTE_ADDRESS";
    private final AuthorizationService authorizationService;
    private final ObjectMapper objectMapper;


    public CustomAuthenticationSuccessHandler(AuthorizationService authorizationService,
                                              ObjectMapper objectMapper) {
        this.authorizationService = authorizationService;
        this.objectMapper = objectMapper;

    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        try {

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            addSessionInformation(request, authentication);

            AuthenticationResponse authResponse = new AuthenticationResponse(userDetails.getUsername());

            authResponse.setPermissions(authorizationService.findAccessPermissionsByUserId(userDetails.getUsername()));

            response.getOutputStream().println(objectMapper.writeValueAsString(authResponse));
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception ex) {

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getOutputStream().println(objectMapper.writeValueAsString(ex.getMessage()));
            log.error(ex.getMessage(), ex);
        }

    }

    private void addSessionInformation(HttpServletRequest request, Authentication authentication) {
        request.getSession(false).setMaxInactiveInterval((int) TimeUnit.MINUTES.toSeconds(15));
        String remoteAddress = ((WebAuthenticationDetails) authentication.getDetails()).getRemoteAddress();
        request.getSession(false).setAttribute(REMOTE_ADDRESS, remoteAddress);
    }

}
