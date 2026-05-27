package com.empik.coupon.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCouponRequest(
        @NotBlank(message = "Coupon code cannot be blank")
        @Size(min = 1, max = 100, message = "Coupon code length should be between 1 and 100 characters")
        String code,

        @Min(value = 1, message = "Max uses should be at least 1")
        @Max(value = 1_000_000, message = "Max uses cannot exceed 1 000 000")
        int maxUses,

        @NotBlank(message = "Country code cannot be blank")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "Country code must be a valid ISO 3166-1 alpha-2 code")
        String countryCode
) {
}
