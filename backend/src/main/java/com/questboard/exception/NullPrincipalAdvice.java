package com.questboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NullPrincipalAdvice {

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleNpe(NullPointerException e, jakarta.servlet.http.HttpServletRequest req) {
        // Se a NPE for "principal is null", devolve 401 em vez de 500
        if (e.getMessage() != null && e.getMessage().contains("principal")) {
            return ErrorResponse.builder()
                    .timestamp(java.time.Instant.now())
                    .status(401)
                    .error("UNAUTHORIZED")
                    .message("Autenticação necessária")
                    .path(req.getRequestURI())
                    .build();
        }
        // repassa NPEs "normais" como 500
        throw e;
    }
}