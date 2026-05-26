package com.empik.coupon.doamin.model;

import com.empik.coupon.doamin.exception.CouponExhaustedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "coupons")
public class CouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "max_uses", nullable = false)
    private int maxUses;

    @Column(name = "current_uses", nullable = false)
    private int currentUses;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    public boolean isExhausted() {
        return currentUses >= maxUses;
    }

    public boolean isForCountry(String userCountryCode) {
        return this.countryCode.equalsIgnoreCase(userCountryCode);
    }

    public void incrementUses() {
        if (isExhausted()) {
            throw new CouponExhaustedException(code);
        }
        this.currentUses++;
    }
}
