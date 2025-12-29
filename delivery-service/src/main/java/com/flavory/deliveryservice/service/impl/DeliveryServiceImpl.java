package com.flavory.deliveryservice.service.impl;

import com.flavory.deliveryservice.dto.request.StuartJobRequest;
import com.flavory.deliveryservice.dto.response.DeliveryResponse;
import com.flavory.deliveryservice.dto.response.DeliverySummaryResponse;
import com.flavory.deliveryservice.dto.response.StuartJobResponse;
import com.flavory.deliveryservice.entity.Delivery;
import com.flavory.deliveryservice.entity.DeliveryAddress;
import com.flavory.deliveryservice.event.inbound.OrderReadyEvent;
import com.flavory.deliveryservice.exception.DeliveryNotFoundException;
import com.flavory.deliveryservice.exception.StuartApiException;
import com.flavory.deliveryservice.exception.UnauthorizedDeliveryAccessException;
import com.flavory.deliveryservice.mapper.DeliveryMapper;
import com.flavory.deliveryservice.repository.DeliveryRepository;
import com.flavory.deliveryservice.security.JwtService;
import com.flavory.deliveryservice.service.DeliveryService;
import com.flavory.deliveryservice.service.stuart.StuartApiService;
import com.flavory.deliveryservice.service.stuart.StuartRequestBuilder;
import com.flavory.deliveryservice.validator.DeliveryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final JwtService jwtService;
    private final StuartApiService stuartApiService;
    private final DeliveryValidator deliveryValidator;
    private final StuartRequestBuilder stuartRequestBuilder;
    private final DeliveryMapper deliveryMapper;

    @Override
    @Transactional
    public void createDeliveryFromEvent(OrderReadyEvent event) {
        if (deliveryRepository.existsByOrderId(event.getOrderId())) {
            return;
        }

        DeliveryAddress pickupAddress = mapPickupAddress(event.getPickupAddress());
        DeliveryAddress dropoffAddress = mapDropoffAddress(event.getDropoffAddress());

        deliveryValidator.validateAddress(pickupAddress, "Pickup");
        deliveryValidator.validateAddress(dropoffAddress, "Dropoff");
        deliveryValidator.validateDeliveryCreation(pickupAddress, dropoffAddress);

        Delivery delivery = Delivery.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .cookId(event.getCookId())
                .status(Delivery.DeliveryStatus.PENDING)
                .pickupAddress(pickupAddress)
                .dropoffAddress(dropoffAddress)
                .build();

        try {
            StuartJobRequest stuartRequest = stuartRequestBuilder.buildJobRequest(
                    event.getOrderId(),
                    pickupAddress,
                    dropoffAddress
            );

            StuartJobResponse stuartResponse = stuartApiService.createJob(stuartRequest);

            delivery.setStuartJobId(stuartResponse.getId().toString());
            delivery.setTrackingUrl(stuartResponse.getTrackingUrl());
            delivery.setStatus(Delivery.DeliveryStatus.SCHEDULED);
            delivery.setEstimatedPickupTime(stuartResponse.getPickupAt());
            delivery.setEstimatedDeliveryTime(stuartResponse.getDropoffAt());

            if (stuartResponse.getPricing() != null) {
                delivery.setDeliveryFee(stuartResponse.getPricing().getPriceTaxIncluded());
            }

            if (stuartResponse.getDistance() != null) {
                delivery.setDistanceKm(stuartResponse.getDistance());
            }

        } catch (StuartApiException e) {
            delivery.setStatus(Delivery.DeliveryStatus.FAILED);
            deliveryRepository.save(delivery);
            throw e;
        }

        delivery = deliveryRepository.save(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(Long orderId, Authentication authentication) {
        String userId = jwtService.extractAuth0Id(authentication);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery for order " + orderId + " not found"));

        validateDeliveryAccess(delivery, userId);

        return deliveryMapper.toResponse(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryById(Long deliveryId, Authentication authentication) {
        String userId = jwtService.extractAuth0Id(authentication);
        Delivery delivery = getDeliveryOrThrow(deliveryId);

        validateDeliveryAccess(delivery, userId);

        return deliveryMapper.toResponse(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeliverySummaryResponse> getCustomerDeliveries(Pageable pageable, Authentication authentication) {
        String customerId = jwtService.extractAuth0Id(authentication);

        Page<Delivery> deliveries = deliveryRepository.findByCustomerId(customerId, pageable);
        return deliveries.map(deliveryMapper::toSummaryResponse);
    }

    @Override
    public Delivery getDeliveryOrThrow(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
    }

    private void validateDeliveryAccess(Delivery delivery, String userId) {
        if (!delivery.getCustomerId().equals(userId) && !delivery.getCookId().equals(userId)) {
            throw new UnauthorizedDeliveryAccessException();
        }
    }

    private DeliveryAddress mapPickupAddress(OrderReadyEvent.PickupAddress address) {
        return DeliveryAddress.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .apartmentNumber(address.getApartmentNumber())
                .phoneNumber(address.getPhoneNumber())
                .contactName(address.getContactName())
                .instructions(address.getInstructions())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }

    private DeliveryAddress mapDropoffAddress(OrderReadyEvent.DropoffAddress address) {
        return DeliveryAddress.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .apartmentNumber(address.getApartmentNumber())
                .phoneNumber(address.getPhoneNumber())
                .contactName(address.getContactName())
                .instructions(address.getInstructions())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }
}
