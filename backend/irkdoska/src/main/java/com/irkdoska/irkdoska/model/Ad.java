package com.irkdoska.irkdoska.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "phone", nullable = false)
    private String phone;

    @ElementCollection
    @CollectionTable(name = "ads_photo_urls", joinColumns = @JoinColumn(name = "ad_id"))
    @Column(name = "photo_url")
    private List<String> photoUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private ModerationStatus moderationStatus = ModerationStatus.PENDING;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Ad(String description, Double price, String city, String phone, User user) {
        this.description = description;
        this.price = price;
        this.city = city;
        this.phone = phone;
        this.user = user;
        this.moderationStatus = ModerationStatus.PENDING;
        this.isPaid = false;
    }

    public Ad(String description, Double price, String city, String phone, User user, Boolean isPaid) {
        this.description = description;
        this.price = price;
        this.city = city;
        this.phone = phone;
        this.user = user;
        this.moderationStatus = ModerationStatus.PENDING;
        this.isPaid = isPaid;
    }
}
