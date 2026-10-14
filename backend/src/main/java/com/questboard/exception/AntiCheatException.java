package com.questboard.exception;

import org.springframework.http.HttpStatus;

public class AntiCheatException extends ApiException {
    public AntiCheatException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}