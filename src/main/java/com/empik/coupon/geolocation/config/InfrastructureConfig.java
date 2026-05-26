package com.empik.coupon.geolocation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class InfrastructureConfig {

    @Bean
    public RestClient geoLocationRestClient(
            @Value("${geolocation.base-url}") String baseUrl,
            @Value("${geolocation.connect-timeout-seconds:5}") int connectTimeoutSeconds,
            @Value("${geolocation.read-timeout-seconds:5}") int readTimeoutSeconds) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutSeconds);
        requestFactory.setReadTimeout(readTimeoutSeconds);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
