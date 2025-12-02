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

    public AdResponse createAd(Long telegramId, String description, Double price, String city, String phone, MultipartFile[] photos) {
        if (description == null || price == null || city == null || phone == null) {
            throw new IllegalArgumentException("Null argument");
        }

        if (description.length() > 200) {
            throw new IllegalArgumentException("Too low argument");
        }

        if (!isPhoneValid(phone)) {
            throw new IllegalArgumentException("Invalid phone number");
        }

        User user = userRepository.findByTelegramId(telegramId).orElseThrow(() -> {
            throw new IllegalArgumentException("Wrong telegram id");
        });
        
        Ad ad = new Ad(description, price, city, phone, user);
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
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));
        ad.setModerationStatus(status);
        adRepository.save(ad);
        log.info("Ad {} moderation status changed to {}", adId, status);
    }

    private boolean isPhoneValid(String phone) {
        String digits = phone.replaceAll("\\D", "");
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
}
