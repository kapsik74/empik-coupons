package com.empik.coupon.doamin.exception;

public class GeolocationException extends RuntimeException {

    public GeolocationException(String ipAddress, String reason) {
        super(String.format("Cannot determine country for IP '%s': %s",  ipAddress, reason));
    }

    public GeolocationException(String ipAddress, Throwable cause) {
        super(String.format("Cannot determine country for IP '%s': %s" , ipAddress, cause));
    }
}
