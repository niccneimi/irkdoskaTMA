package com.irkdoska.irkdoska.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", e.getMessage());
        response.put("message", e.getMessage());
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        if (e.getMessage() != null && e.getMessage().contains("Недостаточно платных объявлений")) {
            status = HttpStatus.PAYMENT_REQUIRED;
        }
        
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Unexpected error", e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Внутренняя ошибка сервера");
        response.put("message", "Произошла ошибка при обработке запроса");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

