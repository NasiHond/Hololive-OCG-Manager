package com.fhict.hololiveocgmanager.exception;

import org.springframework.http.HttpStatus;

import java.net.URI;

public class BadRequestException extends ApiException {
    public BadRequestException(String detail) {
        super(HttpStatus.BAD_REQUEST, URI.create("urn:problem-type:bad-request"), "Bad Request", detail);
    }
}

