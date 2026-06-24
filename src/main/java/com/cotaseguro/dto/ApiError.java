package com.cotaseguro.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> fieldErrors
) {

    public static ApiError of(HttpStatus status, String message, String path) {
        return of(status, message, path, List.of());
    }

    public static ApiError of(HttpStatus status, String message, String path, List<FieldErrorDetail> fieldErrors) {
        return new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors);
    }

}
