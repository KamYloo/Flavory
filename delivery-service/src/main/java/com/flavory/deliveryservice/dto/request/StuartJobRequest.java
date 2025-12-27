package com.flavory.deliveryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StuartJobRequest {

    @JsonProperty("job")
    private Job job;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Job {

        @JsonProperty("assignment_code")
        private String assignmentCode;

        @JsonProperty("pickups")
        private List<Pickup> pickups;

        @JsonProperty("dropoffs")
        private List<Dropoff> dropoffs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pickup {

        @JsonProperty("address")
        private String address;

        @JsonProperty("comment")
        private String comment;

        @JsonProperty("contact")
        private Contact contact;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dropoff {

        @JsonProperty("address")
        private String address;

        @JsonProperty("comment")
        private String comment;

        @JsonProperty("contact")
        private Contact contact;

        @JsonProperty("package_type")
        private String packageType; // "food", "medium", "large"

        @JsonProperty("package_description")
        private String packageDescription;

        @JsonProperty("client_reference")
        private String clientReference;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {

        @JsonProperty("firstname")
        private String firstname;

        @JsonProperty("lastname")
        private String lastname;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("company")
        private String company;
    }
}
