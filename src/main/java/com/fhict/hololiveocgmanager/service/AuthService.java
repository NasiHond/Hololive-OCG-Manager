package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.request.LoginRequest;
import com.fhict.hololiveocgmanager.dto.response.AuthResponse;

public interface AuthService
{
    AuthResponse login(LoginRequest loginRequest);

    AuthResponse validateToken(String token);
}
