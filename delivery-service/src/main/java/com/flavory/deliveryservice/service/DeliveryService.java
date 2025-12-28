package com.flavory.deliveryservice.service;


import com.flavory.deliveryservice.dto.response.DeliveryResponse;
import com.flavory.deliveryservice.entity.Delivery;
import com.flavory.deliveryservice.event.inbound.OrderReadyEvent;
import org.springframework.security.core.Authentication;

public interface  DeliveryService {
    void createDeliveryFromEvent(OrderReadyEvent event);
    DeliveryResponse getDeliveryByOrderId(Long orderId, Authentication authentication);
    Delivery getDeliveryOrThrow(Long deliveryId);
}
