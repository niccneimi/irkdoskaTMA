package com.irkdoska.irkdoska.service;

import com.irkdoska.irkdoska.model.PaidAdPackage;
import com.irkdoska.irkdoska.model.User;
import com.irkdoska.irkdoska.repository.PaidAdPackageRepository;
import com.irkdoska.irkdoska.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaidAdPackageService {

    private final PaidAdPackageRepository packageRepository;
    private final UserRepository userRepository;

    public List<PaidAdPackage> getAvailablePackages() {
        List<PaidAdPackage> packages = packageRepository.findByIsActiveOrderByAdsCountAsc(true);
        log.info("Found {} active packages", packages.size());
        return packages;
    }

    public void purchasePackage(Long telegramId, Long packageId) {
        PaidAdPackage packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found"));

        if (!packageEntity.getIsActive()) {
            throw new IllegalArgumentException("Package is not available");
        }

        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Здесь должна быть интеграция с платежной системой
        
        if (user.getPaidAdsBalance() == null) {
            user.setPaidAdsBalance(0);
        }
        user.setPaidAdsBalance(user.getPaidAdsBalance() + packageEntity.getAdsCount());
        userRepository.save(user);

        log.info("User {} purchased package {} ({} ads for {} rub)", 
                telegramId, packageId, packageEntity.getAdsCount(), packageEntity.getPrice());
    }
}

