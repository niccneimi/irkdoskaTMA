package com.irkdoska.irkdoska.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.irkdoska.irkdoska.security.TmaUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.irkdoska.irkdoska.entity.AdResponse;
import com.irkdoska.irkdoska.entity.AdRequest;
import com.irkdoska.irkdoska.service.AdService;

@Controller
@RequestMapping("/api/ads")
@RestController
@RequiredArgsConstructor
@Slf4j
public class AdController {

    private final AdService adService;

    @GetMapping
    public ResponseEntity<AdResponse> getAllAds(@AuthenticationPrincipal TmaUserPrincipal principal) {
        return ResponseEntity.ok(adService.getAllAds(principal.getTelegramId()));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<AdResponse> createAd(
            @AuthenticationPrincipal TmaUserPrincipal principal,
            @RequestPart("adRequest") AdRequest adRequest,
            @RequestPart(value = "photos", required = false) MultipartFile[] photos) {
        try {
            return ResponseEntity.ok(adService.createAd(
                    principal.getTelegramId(), 
                    adRequest.getDescription(), 
                    adRequest.getPrice(), 
                    adRequest.getCity(), 
                    adRequest.getPhone(), 
                    photos,
                    adRequest.getIsPaid()));
        } catch (IllegalArgumentException e) {
            log.error("Error creating ad: {}", e.getMessage());
            throw e;
        }
    }
}
