package com.empik.coupon.geolocation;

import com.empik.coupon.doamin.exception.GeolocationException;
import com.empik.coupon.geolocation.service.IpApiGeolocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class IpApiGeolocationServiceTest {

    private static final String BASE_URL = "http://ip-api.com";

    private MockRestServiceServer mockServer;
    private IpApiGeolocationService geoLocationService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        geoLocationService = new IpApiGeolocationService(builder.build());
    }

    @Test
    @DisplayName("should return country code when ip-api responds with success")
    void shouldReturnCountryCode() {
        mockServer.expect(requestTo(BASE_URL + "/json/1.2.3.4?fields=status,message,countryCode"))
                .andRespond(withSuccess(
                        """
                        {"status":"success","countryCode":"PL"}
                        """,
                        MediaType.APPLICATION_JSON));

        String countryCode = geoLocationService.getCountryCode("1.2.3.4");

        assertThat(countryCode).isEqualTo("PL");
        mockServer.verify();
    }

    @Test
    @DisplayName("should throw GeoLocationException when ip-api returns failure status")
    void shouldThrowWhenApiReturnsFailure() {
        mockServer.expect(requestTo(BASE_URL + "/json/127.0.0.1?fields=status,message,countryCode"))
                .andRespond(withSuccess(
                        """
                        {"status":"fail","message":"private range"}
                        """,
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> geoLocationService.getCountryCode("127.0.0.1"))
                .isInstanceOf(GeolocationException.class)
                .hasMessageContaining("127.0.0.1");
    }

    @Test
    @DisplayName("should throw GeoLocationException when ip-api returns server error")
    void shouldThrowWhenApiReturnsServerError() {
        mockServer.expect(requestTo(BASE_URL + "/json/1.2.3.4?fields=status,message,countryCode"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> geoLocationService.getCountryCode("1.2.3.4"))
                .isInstanceOf(GeolocationException.class);
    }

    @Test
    @DisplayName("should throw GeoLocationException when ip-api returns empty body")
    void shouldThrowWhenApiReturnsEmptyBody() {
        mockServer.expect(requestTo(BASE_URL + "/json/1.2.3.4?fields=status,message,countryCode"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> geoLocationService.getCountryCode("1.2.3.4"))
                .isInstanceOf(GeolocationException.class);
    }
}
