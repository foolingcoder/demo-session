package com.maureva.demosession.controller;


import com.maureva.demosession.dto.request.SignUpRequest;
import com.maureva.demosession.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authsession")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody SignUpRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok("User Successfully created");
    }

}
