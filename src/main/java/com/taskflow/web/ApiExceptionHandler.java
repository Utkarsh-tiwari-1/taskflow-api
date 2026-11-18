package com.taskflow.web;

import com.taskflow.web.ApiExceptions.Conflict;
import com.taskflow.web.ApiExceptions.Forbidden;
import com.taskflow.web.ApiExceptions.NotFound;
import com.taskflow.web.ApiExceptions.Unauthorized;
import jakarta.persistence.OptimisticLockException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFound.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> notFound(NotFound exception) {
        return response(404, "not_found", exception.getMessage());
    }

    @ExceptionHandler(Conflict.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> conflict(Conflict exception) {
        return response(409, "conflict", exception.getMessage());
    }

    @ExceptionHandler(Forbidden.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> forbidden(Forbidden exception) {
        return response(403, "forbidden", exception.getMessage());
    }

    @ExceptionHandler(Unauthorized.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> unauthorized(Unauthorized exception) {
        return response(401, "unauthorized", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ErrorResponse body = new ErrorResponse(java.time.Instant.now(), 400, "validation_failed",
                "One or more fields are invalid", fields);
        return org.springframework.http.ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException exception) {
        return response(400, "invalid_request", "Request body contains invalid or malformed values");
    }

    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockException.class,
            OptimisticLockingFailureException.class})
    public org.springframework.http.ResponseEntity<ErrorResponse> persistenceConflict(RuntimeException exception) {
        return response(409, "conflict", "The request conflicts with the current data");
    }

    private static org.springframework.http.ResponseEntity<ErrorResponse> response(int status, String error, String message) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.valueOf(status))
                .body(ErrorResponse.of(status, error, message));
    }
}
