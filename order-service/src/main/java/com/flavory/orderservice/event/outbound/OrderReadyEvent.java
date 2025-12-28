package com.flavory.orderservice.event.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadyEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String customerId;
    private String cookId;

    private PickupAddress pickupAddress;
    private DropoffAddress dropoffAddress;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readyAt;

    private String eventId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PickupAddress implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String street;
        private String city;
        private String postalCode;
        private String apartmentNumber;
        private String phoneNumber;
        private String contactName;
        private String instructions;
        private Double latitude;
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DropoffAddress implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String street;
        private String city;
        private String postalCode;
        private String apartmentNumber;
        private String phoneNumber;
        private String contactName;
        private String instructions;
        private Double latitude;
        private Double longitude;
    }
}
