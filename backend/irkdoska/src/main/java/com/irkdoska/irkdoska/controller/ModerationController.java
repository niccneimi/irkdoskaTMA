package com.irkdoska.irkdoska.controller;

import com.irkdoska.irkdoska.model.ModerationStatus;
import com.irkdoska.irkdoska.security.TmaUserPrincipal;
import com.irkdoska.irkdoska.service.AdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
@Slf4j
public class ModerationController {

    private final AdService adService;

    @PostMapping("/{adId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveAd(
            @PathVariable Long adId,
            @AuthenticationPrincipal TmaUserPrincipal principal) {
        log.info("Admin {} approving ad {}", principal.getTelegramId(), adId);
        adService.moderateAd(adId, ModerationStatus.APPROVED);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{adId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectAd(
            @PathVariable Long adId,
            @AuthenticationPrincipal TmaUserPrincipal principal) {
        log.info("Admin {} rejecting ad {}", principal.getTelegramId(), adId);
        adService.moderateAd(adId, ModerationStatus.REJECTED);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bot/{adId}/approve")
    public ResponseEntity<Void> approveAdFromBot(
            @PathVariable Long adId,
            @RequestParam Long telegramId) {
        if (!isAdmin(telegramId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.info("Admin {} approving ad {} from bot", telegramId, adId);
        adService.moderateAd(adId, ModerationStatus.APPROVED);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bot/{adId}/reject")
    public ResponseEntity<Void> rejectAdFromBot(
            @PathVariable Long adId,
            @RequestParam Long telegramId) {
        if (!isAdmin(telegramId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.info("Admin {} rejecting ad {} from bot", telegramId, adId);
        adService.moderateAd(adId, ModerationStatus.REJECTED);
        return ResponseEntity.ok().build();
    }

    private boolean isAdmin(Long telegramId) {
        return telegramId != null && (telegramId == 718802381L || telegramId == 7978201047L);
    }
}

