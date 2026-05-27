package com.empik.coupon.doamin.exception;

public class DuplicateCouponCodeException extends RuntimeException {
    public DuplicateCouponCodeException(String code) {
        super(String.format("Coupon with code: %s already exists", code));
    }
}
