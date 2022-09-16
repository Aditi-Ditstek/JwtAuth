package com.dits.dits.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthenticationResponse {
    private final String jwttoken;
    private final String jwtRefreshToken;

}
