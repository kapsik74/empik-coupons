package com.empik.coupon.api.dto;

import com.empik.coupon.doamin.model.CouponEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCouponResponse(
        UUID id,
        String code,
        LocalDateTime createdAt,
        int maxUses,
        int currentUses,
        String countryCode

) {
    public static CreateCouponResponse from(CouponEntity coupon) {
        return new CreateCouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getCreatedAt(),
                coupon.getMaxUses(),
                coupon.getCurrentUses(),
                coupon.getCountryCode()
        );
    }
}
