package com.maureva.demosession.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maureva.demosession.dto.request.SigninRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.util.Objects;

@Getter
@Setter
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String AUTHENTICATION_METHOD_NOT_SUPPORTED = "Authentication method not supported";
    public static final String REQUEST_BODY_NOT_PROVIDED = "Request body not provided";
    public static final String EMAIL = "email";

    private SessionAuthenticationStrategy authenticatedSessionAuthenticationStrategy;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
            AuthenticationException {

        if (!request.getMethod().equals(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException(AUTHENTICATION_METHOD_NOT_SUPPORTED);
        } else {

            SigninRequest signinRequest;
            try {

                byte[] inputStreamBytes = StreamUtils.copyToByteArray(request.getInputStream());
                signinRequest = new ObjectMapper().readValue(inputStreamBytes, SigninRequest.class);
                request.setAttribute(EMAIL, signinRequest.getEmail());

            } catch (IOException e) {
                throw new AuthenticationServiceException(REQUEST_BODY_NOT_PROVIDED);
            }

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(signinRequest.getEmail(),
                    signinRequest.getPassword()
            );

            setDetails(request, token);

            Authentication authenticationResult = getAuthenticationManager().authenticate(token);
            if (Objects.nonNull(authenticationResult)) {
                setSessionAuthenticationStrategy(authenticatedSessionAuthenticationStrategy);
            }

            return authenticationResult;
        }
    }
    public void setAuthenticatedSessionAuthenticationStrategy(SessionAuthenticationStrategy authenticatedSessionAuthenticationStrategy) {
        this.authenticatedSessionAuthenticationStrategy = authenticatedSessionAuthenticationStrategy;
    }
}
