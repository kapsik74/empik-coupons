package com.empik.coupon.api;

import com.empik.coupon.api.dto.CreateCouponRequest;
import com.empik.coupon.api.dto.CreateCouponResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    }
}
