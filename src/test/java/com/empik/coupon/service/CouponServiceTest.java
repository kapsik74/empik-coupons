package com.empik.coupon.service;

import com.empik.coupon.api.dto.CreateCouponRequest;
import com.empik.coupon.api.dto.CreateCouponResponse;
import com.empik.coupon.api.dto.UseCouponResponse;
import com.empik.coupon.doamin.exception.GeolocationException;
import com.empik.coupon.geolocation.service.GeolocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {

    @Mock
    private CouponCommandService couponCommandService;

    @Mock
    private GeolocationService geolocationService;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("createCoupon should delegate directly to CouponCommandService")
    void createCouponShouldDelegate() {
        //given
        CreateCouponRequest request = new CreateCouponRequest("WIOSNA", 100, "PL");
        CreateCouponResponse expected = new CreateCouponResponse(UUID.randomUUID(), "WIOSNA",
                LocalDateTime.now(), 100, 0, "PL");
        given(couponCommandService.createCoupon(request)).willReturn(expected);

        //when
        CreateCouponResponse result = couponService.createCoupon(request);

        //then
        assertThat(result).isEqualTo(expected);
        then(couponCommandService).should().createCoupon(request);
        then(geolocationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("useCoupon should resolve country before delegating to command service")
    void useCouponShouldResolveCountryBeforeDelegatingToCommandService() {
        //given
        String code = "wiosna";
        String userId = "user1";
        String ip = "1.2.3.4";
        UseCouponResponse expected = UseCouponResponse.success("WIOSNA", LocalDateTime.now());

        given(geolocationService.getCountryCode(ip)).willReturn("PL");
        given(couponCommandService.useCoupon("WIOSNA", userId, "PL", ip)).willReturn(expected);

        //when
        UseCouponResponse result = couponService.useCoupon(code, userId, ip);

        //then
        assertThat(result).isEqualTo(expected);
        InOrder inOrder = Mockito.inOrder(geolocationService, couponCommandService);
        inOrder.verify(geolocationService).getCountryCode(ip);
        inOrder.verify(couponCommandService).useCoupon("WIOSNA", userId, "PL", ip);
    }

    @Test
    @DisplayName("useCoupon should normalize code to uppercase before delegation")
    void useCouponShouldNormalizeCodeToUppercaseBeforeDelegation() {
        //given
        given(geolocationService.getCountryCode("1.2.3.4")).willReturn("PL");
        given(couponCommandService.useCoupon(eq("WIOSNA"), any(), any(), any()))
                .willReturn(UseCouponResponse.success("WIOSNA", LocalDateTime.now()));

        //when
        couponService.useCoupon("wIoSna", "user1", "1.2.3.4");

        //then
        then(couponCommandService).should().useCoupon(eq("WIOSNA"), any(), any(), any());
    }

    @Test
    @DisplayName("useCoupon should propagate GeoLocationException when IP cannot be resolved")
    void useCouponShouldPropagateGeoLocationException() {
        given(geolocationService.getCountryCode("bad-ip"))
                .willThrow(new GeolocationException("bad-ip", "private range"));

        assertThatThrownBy(() -> couponService.useCoupon("WIOSNA", "user1", "bad-ip"))
                .isInstanceOf(GeolocationException.class);

        then(couponCommandService).shouldHaveNoInteractions();
    }
}
