package com.maureva.demosession.service.impl;


import com.maureva.demosession.dto.request.SignUpRequest;
import com.maureva.demosession.entity.User;
import com.maureva.demosession.repository.UserRepository;
import com.maureva.demosession.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(SignUpRequest request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole()).build();

        userRepository.save(user);

    }


}
