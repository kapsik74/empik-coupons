package com.empik.coupon.service;

import com.empik.coupon.api.dto.CreateCouponRequest;
import com.empik.coupon.api.dto.CreateCouponResponse;
import com.empik.coupon.api.dto.UseCouponResponse;
import com.empik.coupon.geolocation.service.GeolocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponCommandService couponCommandService;
    private final GeolocationService geolocationService;

    public CreateCouponResponse createCoupon(CreateCouponRequest createCouponRequest) {
        return couponCommandService.createCoupon(createCouponRequest);
    }

    public UseCouponResponse useCoupon(String couponCode, String userId, String clientIp) {
        String userCountryCode = geolocationService.getCountryCode(clientIp);
        String normalizedCode = couponCode.toUpperCase();
        return couponCommandService.useCoupon(normalizedCode, userId, userCountryCode, clientIp);
    }
}
