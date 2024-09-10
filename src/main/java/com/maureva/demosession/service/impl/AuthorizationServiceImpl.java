package com.maureva.demosession.service.impl;

import com.maureva.demosession.service.AuthorizationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {
    @Override
    public List<String> findAccessPermissionsByUserId(String username) {
        return List.of("ADMIN", "USER");
    }
}
