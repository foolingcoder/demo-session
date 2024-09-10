package com.maureva.demosession.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maureva.demosession.service.AuthorizationService;
import com.maureva.demosession.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private static final String LOGIN_URL = "/authsession/login";
    private static final String LOGOUT_URL = "/authsession/logout";

    private final UserService userService;

    private final AuthorizationService authorizationService;

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request.requestMatchers("/authsession/**")
                        .permitAll().anyRequest().authenticated())

                .headers(headersConfigurer -> headersConfigurer
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                )

                .addFilterBefore(customUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                .logout(logoutCustomizer -> logoutCustomizer
                        .logoutUrl(LOGOUT_URL)
                        .clearAuthentication(true)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                        .addLogoutHandler(customLogoutHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies()
                )


                .addFilterBefore(this.customConcurrentSessionFilter(), ConcurrentSessionFilter.class)

                .sessionManagement(sessionConfigurer -> sessionConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .build();
    }

    @Bean
    public CustomLogoutHandler customLogoutHandler() {
        return new CustomLogoutHandler(sessionRegistry());
    }

    @Bean
    public CustomConcurrentSessionFilter customConcurrentSessionFilter() {
        return new CustomConcurrentSessionFilter(sessionRegistry(), objectMapper);
    }

    @Bean
    public CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter() {
        CustomUsernamePasswordAuthenticationFilter filter = new CustomUsernamePasswordAuthenticationFilter();
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(LOGIN_URL, "POST"));

        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        filter.setAuthenticationManager(authenticationManager());

        filter.setAuthenticatedSessionAuthenticationStrategy(buildAuthenticatedSessionAuthenticationStrategy());

        filter.setSecurityContextRepository(new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        ));

        return filter;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService.userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler(authorizationService, objectMapper);
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler(objectMapper);
    }

    private SessionAuthenticationStrategy buildAuthenticatedSessionAuthenticationStrategy() {

        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlStrategy = new
                ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
        concurrentSessionControlStrategy.setMaximumSessions(1);
        concurrentSessionControlStrategy.setExceptionIfMaximumExceeded(true);

        return new CompositeSessionAuthenticationStrategy(List.of(concurrentSessionControlStrategy,
                new ChangeSessionIdAuthenticationStrategy(),
                new RegisterSessionAuthenticationStrategy(sessionRegistry())));
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

}
