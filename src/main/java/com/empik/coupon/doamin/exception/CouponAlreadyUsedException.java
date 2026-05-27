package com.empik.coupon.doamin.exception;

public class CouponAlreadyUsedException extends RuntimeException {

    public CouponAlreadyUsedException(String code, String userId) {
        super("User: '" + userId + "' has already used coupon: " + code);
    }
}
