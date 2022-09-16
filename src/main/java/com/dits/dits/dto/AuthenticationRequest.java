package com.dits.dits.dto;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String username;
    private String password;
    private  String fullName;
    private boolean isEnabled;
}
