package com.empik.coupon.service;

import com.empik.coupon.api.dto.CreateCouponRequest;
import com.empik.coupon.api.dto.CreateCouponResponse;
import com.empik.coupon.api.dto.UseCouponResponse;
import com.empik.coupon.doamin.exception.CountryNotAllowedException;
import com.empik.coupon.doamin.exception.CouponAlreadyUsedException;
import com.empik.coupon.doamin.exception.CouponNotFoundException;
import com.empik.coupon.doamin.exception.DuplicateCouponCodeException;
import com.empik.coupon.doamin.model.CouponEntity;
import com.empik.coupon.doamin.model.CouponUsageEntity;
import com.empik.coupon.doamin.repository.CouponRepository;
import com.empik.coupon.doamin.repository.CouponUsageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponCommandService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    public CreateCouponResponse createCoupon(CreateCouponRequest request) {
        String normalizedCode = request.code().toUpperCase();

        if (couponRepository.existsByCode(normalizedCode)) {
            throw new DuplicateCouponCodeException(normalizedCode);
        }

        CouponEntity coupon = new CouponEntity();
        coupon.setCode(normalizedCode);
        coupon.setMaxUses(request.maxUses());
        coupon.setCountryCode(request.countryCode().toUpperCase());
        coupon.setCreatedAt(LocalDateTime.now());
        CouponEntity saved = couponRepository.save(coupon);

        return CreateCouponResponse.from(saved);
    }

    public UseCouponResponse useCoupon(String normalizedCode, String userId,
                                       String userCountryCode, String userIp) {
        CouponEntity coupon = couponRepository.findByCodeWithLock(normalizedCode)
                .orElseThrow(() -> new CouponNotFoundException(normalizedCode));

        if (!coupon.isForCountry(userCountryCode)) {
            throw new CountryNotAllowedException(normalizedCode, userCountryCode);
        }

        if (couponUsageRepository.existsByCouponAndUserId(coupon, userId)) {
            throw new CouponAlreadyUsedException(normalizedCode, userId);
        }

        coupon.incrementUses();
        couponRepository.save(coupon);

        CouponUsageEntity usage = new CouponUsageEntity();
        usage.setCoupon(coupon);
        usage.setUserId(userId);
        usage.setUserIp(userIp);
        usage.setUsedAt(LocalDateTime.now());

        couponUsageRepository.save(usage);

        return UseCouponResponse.success(normalizedCode, usage.getUsedAt());
    }
}
