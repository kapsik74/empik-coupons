package com.empik.coupon.doamin.repository;

import com.empik.coupon.doamin.model.CouponEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {

    Optional<CouponEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE c.code = :code")
    Optional<CouponEntity> findByCodeWithLock(@Param("code") String code);
}
