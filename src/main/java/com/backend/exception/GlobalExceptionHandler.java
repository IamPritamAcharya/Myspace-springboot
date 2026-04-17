package com.backend.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler that returns structured JSON error responses
 * instead of raw stack traces.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlatformApiException.class)
    public ResponseEntity<Map<String, Object>> handlePlatformApiException(PlatformApiException ex) {
        log.error("Platform API error [{}]: {}", ex.getPlatform(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "Upstream API failure",
                "platform", ex.getPlatform(),
                "message", ex.getMessage(),
                "status", HttpStatus.BAD_GATEWAY.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error",
                "message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
