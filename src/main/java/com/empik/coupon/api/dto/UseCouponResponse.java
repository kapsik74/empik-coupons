package com.empik.coupon.api.dto;

import java.time.LocalDateTime;

public record UseCouponResponse(
        String code,
        String message,
        LocalDateTime usedAt
) {
    public static UseCouponResponse success(String code, LocalDateTime usedAt) {
        return new UseCouponResponse(code, "Coupon applied successfully", usedAt);
    }
}
