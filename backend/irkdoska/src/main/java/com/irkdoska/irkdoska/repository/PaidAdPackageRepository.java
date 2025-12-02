package com.irkdoska.irkdoska.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.irkdoska.irkdoska.model.PaidAdPackage;
import java.util.List;

@Repository
public interface PaidAdPackageRepository extends JpaRepository<PaidAdPackage, Long> {
    List<PaidAdPackage> findByIsActiveOrderByAdsCountAsc(Boolean isActive);
}

