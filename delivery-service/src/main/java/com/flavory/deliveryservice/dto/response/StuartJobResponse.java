package com.flavory.deliveryservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StuartJobResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("transport_type")
    private String transportType;

    @JsonProperty("package_type")
    private String packageType;

    @JsonProperty("assignment_code")
    private String assignmentCode;

    @JsonProperty("pickup_at")
    private LocalDateTime pickupAt;

    @JsonProperty("dropoff_at")
    private LocalDateTime dropoffAt;

    @JsonProperty("distance")
    private BigDecimal distance;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("pricing")
    private Pricing pricing;

    @JsonProperty("deliveries")
    private List<Delivery> deliveries;

    @JsonProperty("tracking_url")
    private String trackingUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pricing {

        @JsonProperty("price_tax_included")
        private BigDecimal priceTaxIncluded;

        @JsonProperty("currency")
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Delivery {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("status")
        private String status;

        @JsonProperty("driver")
        private Driver driver;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Driver {

        @JsonProperty("firstname")
        private String firstname;

        @JsonProperty("lastname")
        private String lastname;

        @JsonProperty("phone")
        private String phone;
    }
}
