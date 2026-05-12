package com.fhict.hololiveocgmanager.exception;

import org.springframework.http.HttpStatus;

import java.net.URI;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String detail) {
        super(HttpStatus.UNAUTHORIZED, URI.create("urn:problem-type:unauthorized"), "Unauthorized", detail);
    }
}

