package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.request.LoginRequest;
import com.fhict.hololiveocgmanager.dto.response.LoginResponse;

import javax.naming.AuthenticationException;

public interface AuthService
{
    LoginResponse login(LoginRequest loginRequest) throws AuthenticationException;
}
