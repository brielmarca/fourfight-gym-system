package com.gym.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ex.toProblemDetail());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ex.toProblemDetail());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(BusinessRuleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ex.toProblemDetail());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ex.toProblemDetail());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ex.toProblemDetail());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(MethodArgumentNotValidException ex) {
        log.warn("Method argument validation error: {}", ex.getMessage());
        ValidationException validationException = ValidationException.fromMethodArgumentNotValid(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(validationException.toProblemDetail());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch: {}", ex.getMessage());
        ValidationException validationException = ValidationException.fromMethodArgumentTypeMismatch(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(validationException.toProblemDetail());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body");
        problem.setTitle("Bad Request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
        problem.setTitle("Forbidden");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handle(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(problem);
    }
}
