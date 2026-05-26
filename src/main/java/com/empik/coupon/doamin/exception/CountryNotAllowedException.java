package com.empik.coupon.doamin.exception;

public class CountryNotAllowedException extends RuntimeException {

    public CountryNotAllowedException(String code, String userCountry) {
        super("Coupon '" + code + "' is not available in country: " + userCountry);
    }
}
