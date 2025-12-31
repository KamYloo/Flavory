package com.flavory.deliveryservice.service.impl;

import com.flavory.deliveryservice.dto.request.StuartJobRequest;
import com.flavory.deliveryservice.dto.response.DeliveryResponse;
import com.flavory.deliveryservice.dto.response.DeliverySummaryResponse;
import com.flavory.deliveryservice.dto.external.StuartJobResponse;
import com.flavory.deliveryservice.entity.Delivery;
import com.flavory.deliveryservice.entity.DeliveryAddress;
import com.flavory.deliveryservice.event.inbound.OrderReadyEvent;
import com.flavory.deliveryservice.event.outbound.DeliveryCompletedEvent;
import com.flavory.deliveryservice.event.outbound.DeliveryPickedUpEvent;
import com.flavory.deliveryservice.event.outbound.DeliveryStartedEvent;
import com.flavory.deliveryservice.exception.DeliveryNotFoundException;
import com.flavory.deliveryservice.exception.InvalidDeliveryStatusException;
import com.flavory.deliveryservice.exception.StuartApiException;
import com.flavory.deliveryservice.exception.UnauthorizedDeliveryAccessException;
import com.flavory.deliveryservice.mapper.DeliveryMapper;
import com.flavory.deliveryservice.messaging.publisher.DeliveryEventPublisher;
import com.flavory.deliveryservice.repository.DeliveryRepository;
import com.flavory.deliveryservice.security.JwtService;
import com.flavory.deliveryservice.service.DeliveryService;
import com.flavory.deliveryservice.service.stuart.StuartApiService;
import com.flavory.deliveryservice.service.stuart.StuartRequestBuilder;
import com.flavory.deliveryservice.validator.DeliveryValidator;
import com.flavory.deliveryservice.entity.Delivery.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final JwtService jwtService;
    private final StuartApiService stuartApiService;
    private final DeliveryValidator deliveryValidator;
    private final StuartRequestBuilder stuartRequestBuilder;
    private final DeliveryMapper deliveryMapper;
    private final DeliveryEventPublisher deliveryEventPublisher;

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

            if (stuartResponse.getDeliveries() != null && !stuartResponse.getDeliveries().isEmpty()) {
                StuartJobResponse.Delivery stuartDelivery = stuartResponse.getDeliveries().getFirst();
                Long correctId = stuartDelivery.getId();
                delivery.setStuartJobId(correctId.toString());
            }

            delivery.updateStatus(Delivery.DeliveryStatus.SCHEDULED);
            delivery.setEstimatedPickupTime(stuartResponse.getPickupAt());
            delivery.setEstimatedDeliveryTime(stuartResponse.getDropoffAt());

            if (stuartResponse.getPricing() != null) {
                delivery.setDeliveryFee(stuartResponse.getPricing().getPriceTaxIncluded());
            }

            if (stuartResponse.getDistance() != null) {
                delivery.setDistanceKm(stuartResponse.getDistance());
            }

        } catch (StuartApiException e) {
            delivery.updateStatus(Delivery.DeliveryStatus.FAILED);
            deliveryRepository.save(delivery);
            throw e;
        }

        delivery = deliveryRepository.save(delivery);
//        publishDeliveryStartedEvent(delivery);
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
    @Transactional(readOnly = true)
    public Page<DeliverySummaryResponse> getCookDeliveries(Pageable pageable, Authentication authentication) {
        String cookId = jwtService.extractAuth0Id(authentication);
        Page<Delivery> deliveries = deliveryRepository.findByCookId(cookId, pageable);
        return deliveries.map(deliveryMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public void updateDeliveryStatus(String stuartJobId, String newStatus, String courierName, String courierPhone, String trackingUrl) {
        Delivery delivery = deliveryRepository.findByStuartJobId(stuartJobId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found: " + stuartJobId));

        Delivery.DeliveryStatus mappedStatus = mapStuartStatus(newStatus);

        if (mappedStatus == null) {
            return;
        }

        if (courierName != null) {
            delivery.setCourierName(courierName);
        }
        if (courierPhone != null) {
            delivery.setCourierPhone(courierPhone);
        }

        delivery.setTrackingUrl(trackingUrl);

        try {
            delivery.updateStatus(mappedStatus);
        } catch (InvalidDeliveryStatusException e) {
            return;
        }

        switch (mappedStatus) {
//            case PICKED_UP -> {
//                delivery.setActualPickupTime(LocalDateTime.now());
//                publishDeliveryStartedEvent(delivery);
//            }
            case IN_TRANSIT -> {
                if (delivery.getActualPickupTime() == null) {
                    delivery.setActualPickupTime(LocalDateTime.now());
                    publishDeliveryStartedEvent(delivery);
                }
            }
            case DELIVERED -> {
                delivery.setActualDeliveryTime(LocalDateTime.now());
                publishDeliveryCompletedEvent(delivery);
            }
        }

        deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public DeliveryResponse cancelDelivery(Long deliveryId, String reason, Authentication authentication) {
        String userId = jwtService.extractAuth0Id(authentication);
        Delivery delivery = getDeliveryOrThrow(deliveryId);

        validateDeliveryAccess(delivery, userId);

        deliveryValidator.validateDeliveryCancellation(delivery);

        if (delivery.getStuartJobId() != null) {
            try {
                Long stuartJobId = Long.parseLong(delivery.getStuartJobId());
                stuartApiService.cancelJob(stuartJobId);

            } catch (StuartApiException ignored) {
            }
        }

        delivery.updateStatus(Delivery.DeliveryStatus.CANCELLED);
        delivery.setCancellationReason(reason);
        delivery = deliveryRepository.save(delivery);

        return deliveryMapper.toResponse(delivery);
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

    private DeliveryStatus mapStuartStatus(String stuartStatus) {
        if (stuartStatus == null) return null;

        return switch (stuartStatus.toLowerCase()) {
            case "courier_assigned" -> Delivery.DeliveryStatus.COURIER_ASSIGNED;
            case "package_picking_up", "package_delivering" -> Delivery.DeliveryStatus.IN_TRANSIT;
            case "package_picked_up" -> Delivery.DeliveryStatus.PICKED_UP;
            case "package_delivered", "delivered" -> Delivery.DeliveryStatus.DELIVERED;
            case "package_canceled", "canceled", "cancelled" -> Delivery.DeliveryStatus.CANCELLED;

            case "scheduled" -> Delivery.DeliveryStatus.SCHEDULED;
            case "delivering" -> Delivery.DeliveryStatus.IN_TRANSIT;

            default -> {
                System.out.println(">>> [UWAGA] Nieznany status Stuart: " + stuartStatus);
                yield null;
            }
        };
    }

    private void publishDeliveryStartedEvent(Delivery delivery) {
        DeliveryStartedEvent event = DeliveryStartedEvent.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .trackingUrl(delivery.getTrackingUrl())
                .deliveryFee(delivery.getDeliveryFee())
                .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())
                .startedAt(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();

        deliveryEventPublisher.publishDeliveryStarted(event);
    }

    private void publishDeliveryPickedUpEvent(Delivery delivery) {
        DeliveryPickedUpEvent event = DeliveryPickedUpEvent.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .courierName(delivery.getCourierName())
                .courierPhone(delivery.getCourierPhone())
                .pickedUpAt(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();

        deliveryEventPublisher.publishDeliveryPickedUp(event);
    }

    private void publishDeliveryCompletedEvent(Delivery delivery) {
        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .completedAt(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();

        deliveryEventPublisher.publishDeliveryCompleted(event);
    }
}
