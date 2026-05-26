package com.empik.coupon.geolocation.service;

import com.empik.coupon.doamin.exception.GeolocationException;
import com.empik.coupon.geolocation.dto.IpApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class IpApiGeolocationService implements GeolocationService {

    private final RestClient restClient;

    @Override
    public String getCountryCode(String ipAddress) {
        IpApiResponse response;
        try {
            response = restClient.get()
                    .uri("/json/{ip}?fields=status,message,countryCode", ipAddress)
                    .retrieve()
                    .body(IpApiResponse.class);
        } catch (RestClientException e) {
            throw new GeolocationException(ipAddress, e);
        }

        if (response == null || !response.isSuccess()) {
            String reason = response != null ? response.message() : "empty response";
            throw new GeolocationException(ipAddress, reason);
        }

        return response.countryCode();
    }
}
