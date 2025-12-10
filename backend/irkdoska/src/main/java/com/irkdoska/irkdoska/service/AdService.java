package com.irkdoska.irkdoska.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.irkdoska.irkdoska.entity.AdResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.irkdoska.irkdoska.model.Ad;
import com.irkdoska.irkdoska.model.User;
import com.irkdoska.irkdoska.model.ModerationStatus;
import com.irkdoska.irkdoska.repository.AdRepository;
import com.irkdoska.irkdoska.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdService {

    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final MinioStorageService minioStorageService;
    private final TelegramBotService telegramBotService;

    public AdResponse getAllAds(Long telegramId) {
        java.util.List<Ad> ads = adRepository.findByUserTelegramIdOrderByCreatedAtDesc(telegramId);
        return AdResponse.builder()
                .ads(ads)
                .build();
    }

    public AdResponse createAd(Long telegramId, String description, Double price, String city, String phone, MultipartFile[] photos, Boolean isPaid) {
        if (description == null || price == null || city == null || phone == null) {
            throw new IllegalArgumentException("Null argument");
        }

        if (city.length() > 50 || description.length() > 700 || phone.length() > 50 || price.toString().length() > 10) {
            throw new IllegalArgumentException("Too low argument");
        }

        if (!isPhoneOrUsernameValid(phone)) {
            throw new IllegalArgumentException("Invalid phone number or username");
        }

        User user = userRepository.findByTelegramId(telegramId).orElseThrow(() -> {
            throw new IllegalArgumentException("Wrong telegram id");
        });

        if (isPaid != null && isPaid) {
            if (user.getPaidAdsBalance() == null || user.getPaidAdsBalance() <= 0) {
                throw new IllegalArgumentException("Недостаточно платных объявлений. Купите тариф в магазине.");
            }
            user.setPaidAdsBalance(user.getPaidAdsBalance() - 1);
            userRepository.save(user);
        }
        
        if (photos != null && photos.length > 10) {
            throw new IllegalArgumentException("Максимальное количество фото: 10");
        }

        String normalizedContact = normalizePhoneOrUsername(phone);
        Ad ad = new Ad(description, price, city, normalizedContact, user, isPaid != null ? isPaid : false);
        adRepository.save(ad);
        
        if (photos != null && photos.length > 0) {
            java.util.List<String> photoUrls = minioStorageService.uploadPhotos(ad.getId(), photos);
            ad.setPhotoUrls(photoUrls);
            adRepository.save(ad);
        }
        
        telegramBotService.sendAdForModeration(ad);
        
        return AdResponse.builder()
            .ads(List.of(ad))
            .build();
    }

    public void moderateAd(Long adId, ModerationStatus status) {
        moderateAd(adId, status, null);
    }

    public void moderateAd(Long adId, ModerationStatus status, String rejectionReason) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));
        ad.setModerationStatus(status);
        if (rejectionReason != null) {
            ad.setRejectionReason(rejectionReason);
        }
        adRepository.save(ad);
        log.info("Ad {} moderation status changed to {}", adId, status);
            
        if (status == ModerationStatus.APPROVED) {
            telegramBotService.publishAdToChannel(ad);
        } else if (status == ModerationStatus.REJECTED && rejectionReason != null) {
            Long userTelegramId = ad.getUser().getTelegramId();
            telegramBotService.sendNotificationToUser(userTelegramId, rejectionReason);
        }
    }

    private boolean isPhoneOrUsernameValid(String contact) {
        if (contact == null || contact.trim().isEmpty()) {
            return false;
        }
        
        if (contact.trim().startsWith("@")) {
            String username = contact.trim().substring(1);
            return username.matches("^[a-zA-Z0-9_]{5,32}$");
        }
        
        String digits = contact.replaceAll("\\D", "");
        if (digits.length() == 0) return false;
        if (digits.charAt(0) == '7' || digits.charAt(0) == '8') {
            digits = "7" + digits.substring(1);
        } else {
            digits = "7" + digits;
        }
        if (digits.length() > 11) {
            digits = digits.substring(0, 11);
        }
        return digits.length() == 11 && digits.startsWith("7");
    }

    private String normalizePhoneOrUsername(String contact) {
        if (contact == null) {
            return null;
        }

        if (contact.trim().startsWith("@")) {
            return contact.trim();
        }
        return contact.replaceAll("\\s+", "");
    }
}
