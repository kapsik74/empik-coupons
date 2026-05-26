package com.empik.coupon.api;

import com.empik.coupon.api.dto.CreateCouponRequest;
import com.empik.coupon.api.dto.CreateCouponResponse;
import com.empik.coupon.api.dto.UseCouponRequest;
import com.empik.coupon.api.dto.UseCouponResponse;
import com.empik.coupon.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCouponResponse createCoupon(@Valid @RequestBody CreateCouponRequest createCouponRequest) {
        return couponService.createCoupon(createCouponRequest);
    }

    @PostMapping("/{code}/use")
    public UseCouponResponse useCoupon(
            @PathVariable String code,
            @Valid @RequestBody UseCouponRequest useCouponRequest,
            HttpServletRequest httpRequest) {

        String clientIp = extractClientIp(httpRequest);
        return couponService.useCoupon(code, useCouponRequest.userId(), clientIp);
    }


    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
