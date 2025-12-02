package com.irkdoska.irkdoska.controller;

import com.irkdoska.irkdoska.model.PaidAdPackage;
import com.irkdoska.irkdoska.model.User;
import com.irkdoska.irkdoska.repository.UserRepository;
import com.irkdoska.irkdoska.security.TmaUserPrincipal;
import com.irkdoska.irkdoska.service.PaidAdPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@Slf4j
public class ShopController {

    private final PaidAdPackageService packageService;
    private final UserRepository userRepository;

    @GetMapping("/packages")
    public ResponseEntity<List<PaidAdPackage>> getPackages() {
        List<PaidAdPackage> packages = packageService.getAvailablePackages();
        log.info("Returning {} packages", packages.size());
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@AuthenticationPrincipal TmaUserPrincipal principal) {
        User user = userRepository.findByTelegramId(principal.getTelegramId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("balance", user.getPaidAdsBalance() != null ? user.getPaidAdsBalance() : 0);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/purchase/{packageId}")
    public ResponseEntity<Map<String, Object>> purchasePackage(
            @AuthenticationPrincipal TmaUserPrincipal principal,
            @PathVariable Long packageId) {
        try {
            packageService.purchasePackage(principal.getTelegramId(), packageId);
            User user = userRepository.findByTelegramId(principal.getTelegramId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("balance", user.getPaidAdsBalance());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

