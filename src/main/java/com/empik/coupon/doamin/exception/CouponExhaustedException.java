package com.empik.coupon.doamin.exception;

public class CouponExhaustedException extends RuntimeException {
    public CouponExhaustedException(String code) {
        super("Coupon has reached its maximum number of uses: " + code);
    }
}
