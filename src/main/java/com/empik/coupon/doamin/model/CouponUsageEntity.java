package com.empik.coupon.doamin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "coupon_usages")
public class CouponUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private CouponEntity coupon;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @Column(name = "user_ip", nullable = false, length = 45)
    private String userIp;
}
