package com.maureva.demosession.service;


import java.util.List;

public interface AuthorizationService {

    List<String> findAccessPermissionsByUserId(String username);
}
