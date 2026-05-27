package com.empik.coupon.api.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        CouponErrorCode error,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, CouponErrorCode error, String message) {
        return new ErrorResponse(status, error, message, LocalDateTime.now());
    }
}
