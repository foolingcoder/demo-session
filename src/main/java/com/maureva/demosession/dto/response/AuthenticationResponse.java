package com.maureva.demosession.dto.response;

import com.maureva.demosession.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String email;

    private List<String> permissions;

    public AuthenticationResponse(String email) {
        this.email = email;
    }


}
