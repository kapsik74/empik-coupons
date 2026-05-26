package com.empik.coupon.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UseCouponRequest(
        @NotBlank(message = "User ID cannot be blank")
        @Size(min = 1, max = 255, message = "User ID length must be between 1 and 255 characters")
        String userId
) {
}
