package com.empik.coupon.doamin.repository;

import com.empik.coupon.doamin.model.CouponEntity;
import com.empik.coupon.doamin.model.CouponUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponUsageRepository extends JpaRepository<CouponUsageEntity, UUID> {

    boolean existsByCouponAndUserId(CouponEntity coupon, String userId);
}
