package com.irkdoska.irkdoska.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.irkdoska.irkdoska.model.Ad;

@Repository
public interface AdRepository  extends JpaRepository<Ad, Long>{

    java.util.List<Ad> findByUserTelegramIdOrderByCreatedAtDesc(Long telegramId);

}
