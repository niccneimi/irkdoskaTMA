package com.irkdoska.irkdoska.config;

import com.irkdoska.irkdoska.model.PaidAdPackage;
import com.irkdoska.irkdoska.repository.PaidAdPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PaidAdPackageRepository packageRepository;

    @Override
    public void run(String... args) {
        try {
            long count = packageRepository.count();
            log.info("Current paid ad packages count: {}", count);
            
            if (count == 0) {
                log.info("Initializing paid ad packages...");
                
                PaidAdPackage basic = new PaidAdPackage();
                basic.setName("Базовый");
                basic.setAdsCount(1);
                basic.setPrice(100.0);
                basic.setIsActive(true);
                packageRepository.saveAndFlush(basic);
                log.info("Created package: {}", basic.getName());

                PaidAdPackage standard = new PaidAdPackage();
                standard.setName("Стандарт");
                standard.setAdsCount(3);
                standard.setPrice(250.0);
                standard.setIsActive(true);
                packageRepository.saveAndFlush(standard);
                log.info("Created package: {}", standard.getName());

                PaidAdPackage premium = new PaidAdPackage();
                premium.setName("Премиум");
                premium.setAdsCount(5);
                premium.setPrice(400.0);
                premium.setIsActive(true);
                packageRepository.saveAndFlush(premium);
                log.info("Created package: {}", premium.getName());

                log.info("Paid ad packages initialized successfully. Total: {}", packageRepository.count());
            } else {
                log.info("Paid ad packages already exist. Skipping initialization.");
            }
        } catch (Exception e) {
            log.error("Error initializing paid ad packages", e);
        }
    }
}

