package com.flavory.deliveryservice.validator;

import com.flavory.deliveryservice.entity.DeliveryAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DeliveryValidator {

    @Value("${app.business.max-delivery-distance-km}")
    private Double maxDeliveryDistanceKm;

    @Value("${app.business.delivery-timeout-minutes}")
    private Integer deliveryTimeoutMinutes;

    public void validateDeliveryCreation(DeliveryAddress pickupAddress,
                                         DeliveryAddress dropoffAddress) {

        if (pickupAddress == null) {
            throw new IllegalArgumentException("Pickup address is required");
        }

        if (dropoffAddress == null) {
            throw new IllegalArgumentException("Dropoff address is required");
        }

        if (pickupAddress.getLatitude() != null && dropoffAddress.getLatitude() != null) {
            double distance = calculateDistance(
                    pickupAddress.getLatitude(),
                    pickupAddress.getLongitude(),
                    dropoffAddress.getLatitude(),
                    dropoffAddress.getLongitude()
            );

            if (distance > maxDeliveryDistanceKm) {
                throw new IllegalArgumentException(
                        String.format("Delivery distance %.2f km exceeds maximum allowed %d km",
                                distance, maxDeliveryDistanceKm.intValue()));
            }
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    public void validateAddress(DeliveryAddress address, String addressType) {
        if (address == null) {
            throw new IllegalArgumentException(addressType + " address is required");
        }

        if (address.getStreet() == null || address.getStreet().isBlank()) {
            throw new IllegalArgumentException(addressType + " street is required");
        }

        if (address.getCity() == null || address.getCity().isBlank()) {
            throw new IllegalArgumentException(addressType + " city is required");
        }

        if (address.getPostalCode() == null || address.getPostalCode().isBlank()) {
            throw new IllegalArgumentException(addressType + " postal code is required");
        }

        if (address.getPhoneNumber() == null || address.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException(addressType + " phone number is required");
        }

        if (address.getContactName() == null || address.getContactName().isBlank()) {
            throw new IllegalArgumentException(addressType + " contact name is required");
        }
    }
}