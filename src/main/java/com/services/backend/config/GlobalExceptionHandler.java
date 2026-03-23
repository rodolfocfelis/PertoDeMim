package com.services.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ViaCEP or Nominatim unavailable (timeout, connection refused)
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> handleExternalServiceUnavailable(ResourceAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "External service unavailable. Please try again later."));
    }

    // ViaCEP returned 400 — invalid CEP
    @ExceptionHandler(HttpClientErrorException.BadRequest.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(HttpClientErrorException.BadRequest ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid address data provided."));
    }

    // General fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred."));
    }
}