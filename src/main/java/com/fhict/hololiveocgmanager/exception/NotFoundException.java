package com.fhict.hololiveocgmanager.exception;

import org.springframework.http.HttpStatus;

import java.net.URI;

public class NotFoundException extends ApiException {
    public NotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND, URI.create("urn:problem-type:not-found"), "Not Found", detail);
    }
}

