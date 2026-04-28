package com.fhict.hololiveocgmanager.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String expiresIn;
    private String username;
    private String message;
    private Boolean authenticated;
}
