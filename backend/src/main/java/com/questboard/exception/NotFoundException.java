package com.questboard.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public NotFoundException(String resource, Object id) {
        super(HttpStatus.NOT_FOUND, resource + " não encontrado(a): " + id);
    }
}