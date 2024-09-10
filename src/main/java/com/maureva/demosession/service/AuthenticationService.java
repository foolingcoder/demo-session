package com.maureva.demosession.service;


import com.maureva.demosession.dto.request.SignUpRequest;

public interface AuthenticationService {

    void register(SignUpRequest request);
}
