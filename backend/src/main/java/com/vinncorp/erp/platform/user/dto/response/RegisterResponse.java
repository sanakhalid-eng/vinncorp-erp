package com.vinncorp.erp.platform.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private String message;
    private String email;
    private Set<String> roles;

}

