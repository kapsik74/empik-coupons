package com.empik.coupon.doamin.exception;

public class DuplicateCouponCodeException extends RuntimeException {
    public DuplicateCouponCodeException(String code) {
        super("Coupon with code already exists: " + code);
    }
}
