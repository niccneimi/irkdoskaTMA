package com.irkdoska.irkdoska.service;

import com.irkdoska.irkdoska.model.PaidAdPackage;
import com.irkdoska.irkdoska.model.User;
import com.irkdoska.irkdoska.repository.PaidAdPackageRepository;
import com.irkdoska.irkdoska.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaidAdPackageRepository packageRepository;
    private final UserRepository userRepository;
    private final TelegramBotService telegramBotService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${TG_BOT_TOKEN}")
    private String botToken;

    @Value("${YOOMONEY_PROVIDER_TOKEN:}")
    private String yoomoneyProviderToken;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    @Transactional
    public String createInvoiceLink(Long telegramId, Long packageId) {
        PaidAdPackage packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found"));

        if (!packageEntity.getIsActive()) {
            throw new IllegalArgumentException("Package is not available");
        }

        userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String payload = String.format("package_%d_user_%d", packageId, telegramId);
        String apiUrl = TELEGRAM_API_URL + botToken + "/createInvoiceLink";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", "Пакет платных объявлений: " + packageEntity.getName());
        requestBody.put("description", String.format("Количество объявлений: %d", packageEntity.getAdsCount()));
        requestBody.put("payload", payload);
        
        if (yoomoneyProviderToken != null && !yoomoneyProviderToken.trim().isEmpty()) {
            requestBody.put("provider_token", yoomoneyProviderToken.trim());
        }
        
        requestBody.put("currency", "RUB");

        int priceInCents = (int) (packageEntity.getPrice() * 100);
        Map<String, Object> price = new HashMap<>();
        price.put("label", packageEntity.getName());
        price.put("amount", priceInCents);
        requestBody.put("prices", new Object[]{price});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                apiUrl, request, (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if (responseBody.containsKey("ok") && Boolean.FALSE.equals(responseBody.get("ok"))) {
                    String errorDescription = (String) responseBody.getOrDefault("description", "Unknown error");
                    log.error("Telegram API error: {}", errorDescription);
                    throw new RuntimeException("Telegram API error: " + errorDescription);
                }
                
                if (responseBody.containsKey("result")) {
                    String invoiceLink = (String) responseBody.get("result");
                    log.info("Invoice link created for user {} package {}", telegramId, packageId);
                    return invoiceLink;
                }
            }
            
            throw new RuntimeException("Failed to create invoice link");
        } catch (Exception e) {
            log.error("Error creating invoice link", e);
            throw new RuntimeException("Failed to create invoice link: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void processSuccessfulPayment(String invoicePayload, Long telegramId) {
        log.info("Processing successful payment. Invoice payload: {}, Telegram ID: {}", invoicePayload, telegramId);
        
        try {
            if (invoicePayload == null || invoicePayload.trim().isEmpty()) {
                log.error("Invoice payload is null or empty");
                throw new IllegalArgumentException("Invoice payload is required");
            }
            
            if (!invoicePayload.startsWith("package_")) {
                log.error("Invalid invoice payload format: {}", invoicePayload);
                throw new IllegalArgumentException("Invalid invoice payload format: " + invoicePayload);
            }

            String[] parts = invoicePayload.split("_");
            if (parts.length < 4) {
                log.error("Invalid invoice payload format (not enough parts): {}", invoicePayload);
                throw new IllegalArgumentException("Invalid invoice payload format: " + invoicePayload);
            }

            Long packageId;
            try {
                packageId = Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                log.error("Invalid package ID in payload: {}", parts[1]);
                throw new IllegalArgumentException("Invalid package ID in payload: " + parts[1], e);
            }
            
            log.info("Extracted package ID: {} from payload: {}", packageId, invoicePayload);
            
            PaidAdPackage packageEntity = packageRepository.findById(packageId)
                    .orElseThrow(() -> {
                        log.error("Package not found with ID: {}", packageId);
                        return new IllegalArgumentException("Package not found: " + packageId);
                    });
            
            log.info("Found package: {} ({} ads)", packageEntity.getName(), packageEntity.getAdsCount());
            
            User user = userRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> {
                        log.error("User not found with Telegram ID: {}", telegramId);
                        return new IllegalArgumentException("User not found: " + telegramId);
                    });
            
            log.info("Found user: {} (current balance: {})", telegramId, user.getPaidAdsBalance());
            
            if (user.getPaidAdsBalance() == null) {
                user.setPaidAdsBalance(0);
            }
            
            int oldBalance = user.getPaidAdsBalance();
            user.setPaidAdsBalance(user.getPaidAdsBalance() + packageEntity.getAdsCount());
            userRepository.save(user);
            
            log.info("Updated user balance: {} -> {}", oldBalance, user.getPaidAdsBalance());
            
            try {
                telegramBotService.sendNotificationToUser(
                    telegramId,
                    String.format("✅ Платеж успешно выполнен! Вам начислено %d платных объявлений.", 
                        packageEntity.getAdsCount())
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to user, but payment was processed", e);
            }

            log.info("Payment processed successfully. User {} received {} ads", 
                telegramId, packageEntity.getAdsCount());
        } catch (IllegalArgumentException e) {
            log.error("Validation error processing payment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing payment for payload: {}, telegramId: {}", 
                invoicePayload, telegramId, e);
            throw new RuntimeException("Failed to process payment: " + e.getMessage(), e);
        }
    }
}

