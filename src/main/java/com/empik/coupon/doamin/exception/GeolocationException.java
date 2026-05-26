package com.empik.coupon.doamin.exception;

public class GeolocationException extends RuntimeException {

    public GeolocationException(String ipAddress, String reason) {
        super("Cannot determine country for IP '" + ipAddress + "': " + reason);
    }

    public GeolocationException(String ipAddress, Throwable cause) {
        super("Cannot determine country for IP '" + ipAddress + "'", cause);
    }
}
