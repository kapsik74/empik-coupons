package com.empik.coupon.service;

import com.empik.coupon.api.dto.CreateCouponRequest;
import com.empik.coupon.api.dto.CreateCouponResponse;
import com.empik.coupon.api.dto.UseCouponResponse;
import com.empik.coupon.doamin.exception.CountryNotAllowedException;
import com.empik.coupon.doamin.exception.CouponAlreadyUsedException;
import com.empik.coupon.doamin.exception.CouponExhaustedException;
import com.empik.coupon.doamin.exception.CouponNotFoundException;
import com.empik.coupon.doamin.model.CouponEntity;
import com.empik.coupon.doamin.model.CouponUsageEntity;
import com.empik.coupon.doamin.repository.CouponRepository;
import com.empik.coupon.doamin.repository.CouponUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class CouponCommandServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private CouponCommandService couponCommandService;

    @Nested
    @DisplayName("createCoupon")
    class CreateCoupon {

        @Test
        @DisplayName("should create coupon and normalize code to uppercase")
        void shouldCreateCouponWithUppercaseCode() {
            //given
            CreateCouponRequest request = new CreateCouponRequest("wiOsNa", 100, "PL");
            given(couponRepository.existsByCode("WIOSNA")).willReturn(false);
            given(couponRepository.save(any(CouponEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

            //when
            CreateCouponResponse response = couponCommandService.createCoupon(request);

            //then
            assertThat(response.code()).isEqualTo("WIOSNA");
            assertThat(response.maxUses()).isEqualTo(100);
            assertThat(response.currentUses()).isEqualTo(0);
            assertThat(response.countryCode()).isEqualTo("PL");
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("should normalize country code to uppercase")
        void shouldNormalizeCountryCode() {
            //given
            CreateCouponRequest request = new CreateCouponRequest("LATO", 100, "pl");
            given(couponRepository.existsByCode("LATO")).willReturn(false);
            given(couponRepository.save(any(CouponEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

            //when
            CreateCouponResponse response = couponCommandService.createCoupon(request);

            //then
            assertThat(response.countryCode()).isEqualTo("PL");
        }

        @Test
        @DisplayName("should save coupon entity to repository")
        void ShouldPersistCoupon() {
            //given
            CreateCouponRequest request = new CreateCouponRequest("LATO", 50, "DE");
            given(couponRepository.existsByCode("LATO")).willReturn(false);
            given(couponRepository.save(any(CouponEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

            //when
            couponCommandService.createCoupon(request);

            //then
            ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
            then(couponRepository).should().save(captor.capture());
            CouponEntity saved = captor.getValue();
            assertThat(saved.getCode()).isEqualTo("LATO");
            assertThat(saved.getMaxUses()).isEqualTo(50);
            assertThat(saved.getCurrentUses()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("useCoupon")
    class UseCoupon {

        private CouponEntity couponEntity;

        @BeforeEach
        void setUp() {
            couponEntity = new CouponEntity();
            couponEntity.setCode("WIOSNA");
            couponEntity.setMaxUses(10);
            couponEntity.setCountryCode("PL");
            couponEntity.setCreatedAt(LocalDateTime.now());
        }

        @Test
        @DisplayName("should create CouponUsage entity with correct data")
        void shouldCreateCouponUsageEntityWithCorrectData() {
            //given
            given(couponRepository.findByCodeWithLock("WIOSNA")).willReturn(Optional.of(couponEntity));
            given(couponUsageRepository.existsByCouponAndUserId(couponEntity, "user99")).willReturn(false);
            given(couponRepository.save(any(CouponEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(couponUsageRepository.save(any(CouponUsageEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

            //when
            couponCommandService.useCoupon("WIOSNA", "user99", "PL", "5.6.7.8");

            //then
            ArgumentCaptor<CouponUsageEntity> captor = ArgumentCaptor.forClass(CouponUsageEntity.class);
            then(couponUsageRepository).should().save(captor.capture());
            CouponUsageEntity savedUsage = captor.getValue();

            assertThat(savedUsage.getUserId()).isEqualTo("user99");
            assertThat(savedUsage.getUserIp()).isEqualTo("5.6.7.8");
            assertThat(savedUsage.getUsedAt()).isNotNull();
        }

        @Test
        @DisplayName("should use coupon successfully")
        void shouldUseCouponSuccessfully() {
            //given
            given(couponRepository.findByCodeWithLock("WIOSNA")).willReturn(Optional.of(couponEntity));
            given(couponUsageRepository.existsByCouponAndUserId(couponEntity, "user1")).willReturn(false);
            given(couponRepository.save(any(CouponEntity.class))).willAnswer(inv -> inv.getArgument(0));
            given(couponUsageRepository.save(any(CouponUsageEntity.class))).willAnswer(inv -> inv.getArgument(0));

            //when
            UseCouponResponse response = couponCommandService.useCoupon("WIOSNA", "user1", "PL", "1.2.3.4");

            //then
            assertThat(response.code()).isEqualTo("WIOSNA");
            assertThat(response.message()).contains("successfully");
            assertThat(response.usedAt()).isNotNull();
            assertThat(couponEntity.getCurrentUses()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw CouponNotFoundException when coupon does not exist")
        void shouldThrowCouponNotFound() {
            //given
            given(couponRepository.findByCodeWithLock("UNKNOWN")).willReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> couponCommandService.useCoupon("UNKNOWN", "user1", "PL", "1.2.3.4"))
                    .isExactlyInstanceOf(CouponNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");

        }

        @Test
        @DisplayName("should throw CountryNotAllowedException when user is from wrong country")
        void shouldThrowWhenCountryNotAllowed() {
            //given
            given(couponRepository.findByCodeWithLock("WIOSNA")).willReturn(Optional.of(couponEntity));

            //when
            assertThatThrownBy(() -> couponCommandService.useCoupon("WIOSNA", "user1", "DE", "1.2.3.4"))
                    .isExactlyInstanceOf(CountryNotAllowedException.class)
                    .hasMessageContaining("DE");

            //then
            then(couponUsageRepository).should(never()).existsByCouponAndUserId(any(), any());
        }

        @Test
        @DisplayName("should throw CouponAlreadyUsedException when user already used coupon")
        void shouldThrowWhenAlreadyUsed() {
            //given
            given(couponRepository.findByCodeWithLock("WIOSNA")).willReturn(Optional.of(couponEntity));
            given(couponUsageRepository.existsByCouponAndUserId(couponEntity, "user1")).willReturn(true);

            //when
            assertThatThrownBy(() -> couponCommandService.useCoupon("WIOSNA", "user1", "PL", "1.2.3.4"))
                    .isExactlyInstanceOf(CouponAlreadyUsedException.class)
                    .hasMessageContaining("user1");

            //then
            then(couponRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("should throw CouponExhaustedException when coupon reached max uses")
        void shouldThrowWhenCouponExhausted() {
            //given
            CouponEntity exhaustedCoupon = new CouponEntity();
            exhaustedCoupon.setCode("WIOSNA");
            exhaustedCoupon.setMaxUses(1);
            exhaustedCoupon.setCountryCode("PL");
            exhaustedCoupon.setCreatedAt(LocalDateTime.now());

            exhaustedCoupon.incrementUses();

            given(couponRepository.findByCodeWithLock("WIOSNA")).willReturn(Optional.of(exhaustedCoupon));
            given(couponUsageRepository.existsByCouponAndUserId(exhaustedCoupon, "user2")).willReturn(false);

            //when & then
            assertThatThrownBy(() -> couponCommandService.useCoupon("WIOSNA", "user2", "PL", "1.2.3.4"))
                    .isExactlyInstanceOf(CouponExhaustedException.class)
                    .hasMessageContaining("WIOSNA");
        }
    }
}
