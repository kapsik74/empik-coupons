package com.empik.coupon.geolocation.service;

import com.empik.coupon.doamin.exception.GeolocationException;

public interface GeolocationService {

    /**
     * Returns ISO 3166-1 alpha-2 country code for the given IP address.
     *
     * @throws GeolocationException if country cannot be determined
     */
    String getCountryCode(String ipAddress);
}
