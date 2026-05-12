package com.fhict.hololiveocgmanager.exception;

import org.springframework.http.HttpStatus;

import java.net.URI;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String detail) {
        super(HttpStatus.FORBIDDEN, URI.create("urn:problem-type:forbidden"), "Forbidden", detail);
    }
}

