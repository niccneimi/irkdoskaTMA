package com.irkdoska.irkdoska.controller;

import com.irkdoska.irkdoska.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${PAYMENT_WEBHOOK_SECRET:}")
    private String paymentWebhookSecret;

    @PostMapping("/success")
    public ResponseEntity<Map<String, String>> processPayment(
            @RequestHeader(value = "X-Payment-Token", required = false) String token,
            @RequestBody Map<String, Object> request) {
        
        if (paymentWebhookSecret == null || paymentWebhookSecret.trim().isEmpty()) {
            log.error("PAYMENT_WEBHOOK_SECRET not configured");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Server configuration error"));
        }
        
        if (token == null || !token.equals(paymentWebhookSecret)) {
            log.warn("Invalid or missing payment webhook token. IP: {}", 
                request.get("_remote_ip") != null ? request.get("_remote_ip") : "unknown");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", "error", "message", "Unauthorized"));
        }
        log.info("Received payment success request: {}", request);
        
        try {
            String invoicePayload = (String) request.get("invoice_payload");
            Object telegramIdObj = request.get("telegram_id");
            
            if (invoicePayload == null || invoicePayload.trim().isEmpty()) {
                log.error("Missing or empty invoice_payload in request");
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "invoice_payload is required"));
            }
            
            if (telegramIdObj == null) {
                log.error("Missing telegram_id in request");
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "telegram_id is required"));
            }
            
            Long telegramId;
            try {
                telegramId = Long.parseLong(telegramIdObj.toString());
            } catch (NumberFormatException e) {
                log.error("Invalid telegram_id format: {}", telegramIdObj);
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Invalid telegram_id format"));
            }
            
            log.info("Processing payment. Invoice payload: {}, Telegram ID: {}", invoicePayload, telegramId);
            
            paymentService.processSuccessfulPayment(invoicePayload, telegramId);
            
            log.info("Payment processed successfully");
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (IllegalArgumentException e) {
            log.error("Validation error processing payment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing payment", e);
            return ResponseEntity.status(500)
                .body(Map.of("status", "error", "message", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }
}

