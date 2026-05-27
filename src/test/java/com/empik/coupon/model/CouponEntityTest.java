package com.empik.coupon.model;

import com.empik.coupon.doamin.exception.CouponExhaustedException;
import com.empik.coupon.doamin.model.CouponEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Coupon model")
public class CouponEntityTest {

    @Test
    @DisplayName("should start with zero current uses")
    void shouldStartWithZeroUses() {
        CouponEntity coupon = prepareCouponEntity("TEST", 10, "PL");
        assertThat(coupon.getCurrentUses()).isZero();
        assertThat(coupon.isExhausted()).isFalse();
    }

    @Test
    @DisplayName("should report exhausted when current uses equal max uses")
    void shouldReportExhaustedAtMaxUses() {
        CouponEntity coupon = prepareCouponEntity("TEST", 2, "PL");
        coupon.incrementUses();
        coupon.incrementUses();

        assertThat(coupon.isExhausted()).isTrue();
    }

    @Test
    @DisplayName("should throw CouponExhaustedException when incrementing beyond max")
    void shouldThrowWhenIncrementingBeyondMax() {
        CouponEntity coupon = prepareCouponEntity("TEST", 1, "PL");
        coupon.incrementUses();

        assertThatThrownBy(coupon::incrementUses)
                .isInstanceOf(CouponExhaustedException.class);
    }

    @Test
    @DisplayName("isForCountry should be case-insensitive")
    void isForCountryShouldBeCaseInsensitive() {
        CouponEntity coupon = prepareCouponEntity("TEST", 10, "PL");

        assertThat(coupon.isForCountry("PL")).isTrue();
        assertThat(coupon.isForCountry("pl")).isTrue();
        assertThat(coupon.isForCountry("Pl")).isTrue();
        assertThat(coupon.isForCountry("DE")).isFalse();
    }

    @Test
    @DisplayName("incrementUses should increase current uses by one")
    void shouldIncrementUsesByOne() {
        CouponEntity coupon = prepareCouponEntity("TEST", 10, "PL");

        coupon.incrementUses();
        assertThat(coupon.getCurrentUses()).isEqualTo(1);

        coupon.incrementUses();
        assertThat(coupon.getCurrentUses()).isEqualTo(2);
    }

    private CouponEntity prepareCouponEntity(String code, int maxUses, String countryCode) {
        CouponEntity coupon = new CouponEntity();
        coupon.setCode(code);
        coupon.setCountryCode(countryCode);
        coupon.setMaxUses(maxUses);
        coupon.setCurrentUses(0);
        coupon.setCreatedAt(LocalDateTime.now());

        return coupon;
    }
}
