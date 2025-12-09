package com.irkdoska.irkdoska.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

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

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, Object>> handleMultipartException(MultipartException e) {
        log.error("MultipartException: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        String message = "Размер файлов слишком большой. Максимальный размер одного файла: 20MB, общий размер запроса: 200MB";
        
        if (e.getMessage() != null && e.getMessage().contains("exceeded")) {
            message = "Превышен максимальный размер файлов. Максимальный размер одного файла: 20MB, общий размер всех файлов: 200MB";
        }
        
        response.put("error", message);
        response.put("message", message);
        
        return ResponseEntity.status(413).body(response);
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

