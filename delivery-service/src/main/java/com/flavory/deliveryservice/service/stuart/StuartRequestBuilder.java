package com.flavory.deliveryservice.service.stuart;

import com.flavory.deliveryservice.dto.request.StuartJobRequest;
import com.flavory.deliveryservice.entity.DeliveryAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StuartRequestBuilder {
    public StuartJobRequest buildJobRequest(Long orderId,
                                            DeliveryAddress pickupAddress,
                                            DeliveryAddress dropoffAddress) {

        StuartJobRequest.Pickup pickup = StuartJobRequest.Pickup.builder()
                .address(formatAddress(pickupAddress))
                .comment(pickupAddress.getInstructions())
                .contact(StuartJobRequest.Contact.builder()
                        .firstname(extractFirstName(pickupAddress.getContactName()))
                        .lastname(extractLastName(pickupAddress.getContactName()))
                        .phone(pickupAddress.getPhoneNumber())
                        .company("Flavory Kitchen")
                        .build())
                .latitude(pickupAddress.getLatitude())
                .longitude(pickupAddress.getLongitude())
                .build();

        StuartJobRequest.Dropoff dropoff = StuartJobRequest.Dropoff.builder()
                .address(formatAddress(dropoffAddress))
                .comment(dropoffAddress.getInstructions())
                .contact(StuartJobRequest.Contact.builder()
                        .firstname(extractFirstName(dropoffAddress.getContactName()))
                        .lastname(extractLastName(dropoffAddress.getContactName()))
                        .phone(dropoffAddress.getPhoneNumber())
                        .build())
                .packageType("medium")
                .packageDescription("Order #" + orderId + " - Home-cooked meal")
                .clientReference("ORDER_" + orderId)
                .latitude(dropoffAddress.getLatitude())
                .longitude(dropoffAddress.getLongitude())
                .build();

        StuartJobRequest.Job job = StuartJobRequest.Job.builder()
                .assignmentCode("FLAVORY_ORDER_" + orderId)
                .pickups(List.of(pickup))
                .dropoffs(List.of(dropoff))
                .build();

        return StuartJobRequest.builder()
                .job(job)
                .build();
    }

    private String formatAddress(DeliveryAddress address) {
        StringBuilder sb = new StringBuilder();

        sb.append(address.getStreet());

        if (address.getApartmentNumber() != null && !address.getApartmentNumber().isBlank()) {
            sb.append(", apt ").append(address.getApartmentNumber());
        }

        sb.append(", ").append(address.getPostalCode());
        sb.append(" ").append(address.getCity());

        return sb.toString();
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Customer";
        }

        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }

        return "";
    }
}
