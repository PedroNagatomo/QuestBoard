package com.questboard.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors,
        Map<String, Object> details
) {
    @Builder
    public record FieldError(String field, String message, Object rejectedValue) {}
}