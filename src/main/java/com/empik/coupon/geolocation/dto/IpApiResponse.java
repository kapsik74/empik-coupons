package com.empik.coupon.geolocation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IpApiResponse(
        String status,
        String countryCode,
        String message
) {
    public boolean isSuccess() {
        return "success".equals(status);
    }
}
