package com.empik.coupon.api;

import com.empik.coupon.api.dto.CreateCouponRequest;
import com.empik.coupon.api.dto.CreateCouponResponse;
import com.empik.coupon.api.dto.UseCouponResponse;
import com.empik.coupon.doamin.exception.GeolocationException;
import com.empik.coupon.geolocation.service.GeolocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static com.empik.coupon.api.dto.CouponErrorCode.COUNTRY_NOT_ALLOWED;
import static com.empik.coupon.api.dto.CouponErrorCode.COUPON_ALREADY_USED;
import static com.empik.coupon.api.dto.CouponErrorCode.COUPON_CODE_DUPLICATE;
import static com.empik.coupon.api.dto.CouponErrorCode.COUPON_EXHAUSTED;
import static com.empik.coupon.api.dto.CouponErrorCode.COUPON_NOT_FOUND;
import static com.empik.coupon.api.dto.CouponErrorCode.GEO_LOCATION_UNAVAILABLE;
import static com.empik.coupon.api.dto.CouponErrorCode.VALIDATION_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CouponControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GeolocationService geoLocationService;

    private MockMvc mockMvc;

    private static final String COUPONS_URL = "/api/coupons";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("POST /api/coupons — create coupon")
    class CreateCoupon {
        @Test
        @DisplayName("should create coupon and return 201 with coupon details")
        void shouldCreateCoupon() throws Exception {
            //given
            CreateCouponRequest request = new CreateCouponRequest("SPRING2024", 100, "PL");

            //when
            MvcResult result = mockMvc.perform(post(COUPONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            //then
            String json = result.getResponse().getContentAsString();
            CreateCouponResponse response = objectMapper.readValue(json, CreateCouponResponse.class);

            assertThat(response.code()).isEqualTo("SPRING2024");
            assertThat(response.maxUses()).isEqualTo(100);
            assertThat(response.currentUses()).isEqualTo(0);
            assertThat(response.countryCode()).isEqualTo("PL");
            assertThat(response.id()).isNotNull();
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("should normalize coupon code to uppercase")
        void shouldNormalizeCodeToUppercase() throws Exception {
            //given
            CreateCouponRequest request = new CreateCouponRequest("wiosna", 50, "PL");

            //when
            MvcResult resul = mockMvc.perform(post(COUPONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            //then
            String json = resul.getResponse().getContentAsString();
            CreateCouponResponse response = objectMapper.readValue(json, CreateCouponResponse.class);

            assertThat(response.code()).isEqualTo("WIOSNA");
        }

        @Test
        @DisplayName("should return 409 when coupon code already exists (case-insensitive)")
        void shouldReturn409WhenCodeDuplicate() throws Exception {
            //given
            CreateCouponRequest first = new CreateCouponRequest("SUMMER2024", 10, "DE");
            CreateCouponRequest duplicate = new CreateCouponRequest("summer2024", 20, "DE");

            //when & then
            mockMvc.perform(post(COUPONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(first)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(COUPONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicate)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value(COUPON_CODE_DUPLICATE.name()));
        }

        @Test
        @DisplayName("should return 400 when request (as a contract) is invalid")
        void shouldReturn400WhenInvalidRequest() throws Exception {
            //given
            String invalidJson = """
                    {"code": "", "maxUses": 0, "countryCode": "INVALID"}
                    """;

            //when & then
            mockMvc.perform(post(COUPONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(VALIDATION_FAILED.name()));
        }

        @Test
        @DisplayName("should return 400 when country code format is wrong")
        void shouldReturn400WhenCountryCodeInvalid() throws Exception {
            //given
            String json = """
                    {"code": "TEST", "maxUses": 10, "countryCode": "POL"}
                    """;

            //when & then
            mockMvc.perform(post(COUPONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/coupons/{code}/use — use coupon")
    class UseCoupon {

        @Test
        @DisplayName("should use coupon successfully and return 200")
        void shouldUseCouponSuccessfully() throws Exception {
            //given
            createCoupon("COUPON_USE_OK", 10, "PL");
            given(geoLocationService.getCountryCode(anyString())).willReturn("PL");

            //when
            MvcResult result = mockMvc.perform(post(COUPONS_URL + "/COUPON_USE_OK/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "user1"}
                                    """))
                    .andExpect(status().isOk())
                    .andReturn();

            //then
            String json = result.getResponse().getContentAsString();
            UseCouponResponse response = objectMapper.readValue(json, UseCouponResponse.class);

            assertThat(response.code()).isEqualTo("COUPON_USE_OK");
            assertThat(response.message()).contains("successfully");
            assertThat(response.usedAt()).isNotNull();
        }

        @Test
        @DisplayName("should use coupon with lowercase code (case-insensitive)")
        void shouldUseCouponCaseInsensitive() throws Exception {
            //given
            createCoupon("AUTUMN2024", 5, "GB");
            given(geoLocationService.getCountryCode(anyString())).willReturn("GB");

            //when & then
            mockMvc.perform(post(COUPONS_URL + "/autumn2024/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "user1"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("AUTUMN2024"));
        }

        @Test
        @DisplayName("should return 404 when coupon code does not exist")
        void shouldReturn404WhenCouponNotFound() throws Exception {
            //given
            given(geoLocationService.getCountryCode(anyString())).willReturn("PL");

            //when & then
            mockMvc.perform(post(COUPONS_URL + "/NONEXISTENT/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "user1"}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(COUPON_NOT_FOUND.name()));
        }

        @Test
        @DisplayName("should return 403 when user's country does not match coupon country")
        void shouldReturn403WhenCountryNotAllowed() throws Exception {
            //given
            createCoupon("PL_ONLY_COUPON", 10, "PL");
            given(geoLocationService.getCountryCode(anyString())).willReturn("DE");

            //when & then
            mockMvc.perform(post(COUPONS_URL + "/PL_ONLY_COUPON/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "userDE"}
                                    """))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value(COUNTRY_NOT_ALLOWED.name()));
        }

        @Test
        @DisplayName("should return 409 when user already used the coupon")
        void shouldReturn409WhenAlreadyUsed() throws Exception {
            //given
            createCoupon("ONCE_ONLY_COUPON", 10, "PL");
            given(geoLocationService.getCountryCode(anyString())).willReturn("PL");

            //when & then
            mockMvc.perform(post(COUPONS_URL + "/ONCE_ONLY_COUPON/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "repeatUser"}
                                    """))
                    .andExpect(status().isOk());

            mockMvc.perform(post(COUPONS_URL + "/ONCE_ONLY_COUPON/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "repeatUser"}
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value(COUPON_ALREADY_USED.name()));
        }

        @Test
        @DisplayName("should return 422 when coupon has reached max uses")
        void shouldReturn422WhenCouponExhausted() throws Exception {
            //given
            createCoupon("EXHAUST_TEST", 1, "PL");
            given(geoLocationService.getCountryCode(anyString())).willReturn("PL");

            //when & then
            mockMvc.perform(post(COUPONS_URL + "/EXHAUST_TEST/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "user1"}
                                    """))
                    .andExpect(status().isOk());

            mockMvc.perform(post(COUPONS_URL + "/EXHAUST_TEST/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "user2"}
                                    """))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.error").value(COUPON_EXHAUSTED.name()));
        }

        @Test
        @DisplayName("should return 502 when geo-location service is unavailable")
        void shouldReturn502WhenGeoLocationFails() throws Exception {
            //givne
            createCoupon("GEO_FAIL_COUPON", 10, "PL");
            given(geoLocationService.getCountryCode(anyString()))
                    .willThrow(new GeolocationException("1.2.3.4", "service timeout"));

            //when & then
            mockMvc.perform(post(COUPONS_URL + "/GEO_FAIL_COUPON/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "user1"}
                                    """))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.error").value(GEO_LOCATION_UNAVAILABLE.name()));
        }

        @Test
        @DisplayName("should allow different users to use the same coupon")
        void shouldAllowDifferentUsersToUseSameCoupon() throws Exception {
            //givne
            createCoupon("MULTI_USER_COUPON", 5, "PL");
            given(geoLocationService.getCountryCode(anyString())).willReturn("PL");

            //when & then
            mockMvc.perform(post(COUPONS_URL + "/MULTI_USER_COUPON/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "userA"}
                                    """))
                    .andExpect(status().isOk());

            mockMvc.perform(post(COUPONS_URL + "/MULTI_USER_COUPON/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": "userB"}
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 400 when userId is blank")
        void shouldReturn400WhenUserIdBlank() throws Exception {
            mockMvc.perform(post(COUPONS_URL + "/SOME_CODE/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId": ""}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(VALIDATION_FAILED.name()));
        }

        private void createCoupon(String code, int maxUses, String countryCode) throws Exception {
            CreateCouponRequest request = new CreateCouponRequest(code, maxUses, countryCode);
            mockMvc.perform(post(COUPONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }
}
