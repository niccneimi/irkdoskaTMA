package com.irkdoska.irkdoska.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "paid_ad_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaidAdPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ads_count", nullable = false)
    private Integer adsCount;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

