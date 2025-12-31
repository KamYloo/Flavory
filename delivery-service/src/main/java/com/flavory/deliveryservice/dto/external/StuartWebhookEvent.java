package com.flavory.deliveryservice.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StuartWebhookEvent {

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("details")
    private Details details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Details {

        @JsonProperty("package")
        private PackageData packageData;

        @JsonProperty("courier")
        private Courier courier;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackageData {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("end_customer_tracking_url")
        private String endCustomerTrackingUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Courier {
        @JsonProperty("name")
        private String name;

        @JsonProperty("phone")
        private String phone;
    }
}