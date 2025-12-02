package com.irkdoska.irkdoska.service;

import com.irkdoska.irkdoska.model.Ad;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${DOMAIN_NAME}")
    private String domainName;

    @Value("${TG_BOT_TOKEN}")
    private String botToken;

    private static final Set<Long> ADMIN_IDS = Set.of(718802381L, 7978201047L);
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    public void sendAdForModeration(Ad ad) {
        try {
            String text = formatAdText(ad);
            List<String> photoUrls = ad.getPhotoUrls();
            
            if (photoUrls != null && !photoUrls.isEmpty()) {
                sendPhotoWithCaption(ad.getId(), text, photoUrls);
            } else {
                sendTextMessage(text, ad.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send ad for moderation, adId={}", ad.getId(), e);
        }
    }

    private String formatAdText(Ad ad) {
        StringBuilder sb = new StringBuilder();
        sb.append(ad.getDescription()).append("\n\n");
        sb.append("💵Цена: ").append(ad.getPrice()).append("₽\n");
        sb.append("🏙Город: ").append(ad.getCity()).append("\n");
        sb.append("📞Номер: ").append(ad.getPhone());
        return sb.toString();
    }

    private void sendPhotoWithCaption(Long adId, String caption, List<String> photoPaths) {
        if (photoPaths.size() == 1) {
            sendSinglePhoto(adId, caption, photoPaths.get(0));
        } else {
            sendMediaGroup(adId, caption, photoPaths);
        }
    }

    private void sendSinglePhoto(Long adId, String caption, String photoPath) {
        String apiUrl = TELEGRAM_API_URL + botToken + "/sendPhoto";
        
        for (Long adminId : ADMIN_IDS) {
            try {
                String photoUrl = buildPhotoUrl(photoPath);
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("chat_id", adminId);
                requestBody.put("photo", photoUrl);
                requestBody.put("caption", caption);
                
                Map<String, Object> keyboard = createInlineKeyboard(adId);
                requestBody.put("reply_markup", keyboard);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                restTemplate.postForEntity(apiUrl, request, Map.class);
                log.info("Sent ad {} for moderation to admin {}", adId, adminId);
            } catch (Exception e) {
                log.error("Failed to send photo to admin {}, adId={}", adminId, adId, e);
            }
        }
    }

    private void sendMediaGroup(Long adId, String caption, List<String> photoPaths) {
        String apiUrl = TELEGRAM_API_URL + botToken + "/sendMediaGroup";
        
        for (Long adminId : ADMIN_IDS) {
            try {
                List<Map<String, Object>> media = new ArrayList<>();
                
                for (int i = 0; i < photoPaths.size(); i++) {
                    String photoUrl = buildPhotoUrl(photoPaths.get(i));
                    Map<String, Object> photoItem = new HashMap<>();
                    photoItem.put("type", "photo");
                    photoItem.put("media", photoUrl);
                    
                    if (i == photoPaths.size() - 1) {
                        photoItem.put("caption", caption);
                    }
                    
                    media.add(photoItem);
                }
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("chat_id", adminId);
                requestBody.put("media", media);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                restTemplate.postForEntity(apiUrl, request, Map.class);
                
                sendButtonsAfterMediaGroup(adminId, adId);
                
                log.info("Sent ad {} with {} photos for moderation to admin {}", adId, photoPaths.size(), adminId);
            } catch (Exception e) {
                log.error("Failed to send media group to admin {}, adId={}", adminId, adId, e);
            }
        }
    }

    private void sendButtonsAfterMediaGroup(Long adminId, Long adId) {
        try {
            String apiUrl = TELEGRAM_API_URL + botToken + "/sendMessage";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("chat_id", adminId);
            requestBody.put("text", "Модерация объявления:");
            
            Map<String, Object> keyboard = createInlineKeyboard(adId);
            requestBody.put("reply_markup", keyboard);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            restTemplate.postForEntity(apiUrl, request, Map.class);
        } catch (Exception e) {
            log.error("Failed to send buttons after media group to admin {}, adId={}", adminId, adId, e);
        }
    }

    private Map<String, Object> createInlineKeyboard(Long adId) {
        Map<String, Object> keyboard = new HashMap<>();
        List<List<Map<String, String>>> inlineKeyboard = new ArrayList<>();
        
        List<Map<String, String>> row1 = new ArrayList<>();
        Map<String, String> approveBtn = new HashMap<>();
        approveBtn.put("text", "✅ Одобрить");
        approveBtn.put("callback_data", "approve_" + adId);
        row1.add(approveBtn);
        
        Map<String, String> rejectBtn = new HashMap<>();
        rejectBtn.put("text", "❌ Отклонить");
        rejectBtn.put("callback_data", "reject_" + adId);
        row1.add(rejectBtn);
        
        inlineKeyboard.add(row1);
        keyboard.put("inline_keyboard", inlineKeyboard);
        return keyboard;
    }

    private void sendTextMessage(String text, Long adId) {
        String apiUrl = TELEGRAM_API_URL + botToken + "/sendMessage";
        
        for (Long adminId : ADMIN_IDS) {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("chat_id", adminId);
                requestBody.put("text", text);
                
                Map<String, Object> keyboard = createInlineKeyboard(adId);
                requestBody.put("reply_markup", keyboard);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                restTemplate.postForEntity(apiUrl, request, Map.class);
                log.info("Sent ad {} for moderation to admin {}", adId, adminId);
            } catch (Exception e) {
                log.error("Failed to send message to admin {}, adId={}", adminId, adId, e);
            }
        }
    }

    private String buildPhotoUrl(String photoPath) {
        return "https://" + domainName + "/api/photos?path=" + 
               java.net.URLEncoder.encode(photoPath, java.nio.charset.StandardCharsets.UTF_8);
    }
}

