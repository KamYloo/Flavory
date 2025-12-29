package com.flavory.deliveryservice.service;


import com.flavory.deliveryservice.dto.response.DeliveryResponse;
import com.flavory.deliveryservice.dto.response.DeliverySummaryResponse;
import com.flavory.deliveryservice.entity.Delivery;
import com.flavory.deliveryservice.event.inbound.OrderReadyEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface  DeliveryService {
    void createDeliveryFromEvent(OrderReadyEvent event);
    DeliveryResponse getDeliveryByOrderId(Long orderId, Authentication authentication);
    DeliveryResponse getDeliveryById(Long deliveryId, Authentication authentication);
    Page<DeliverySummaryResponse> getCustomerDeliveries(Pageable pageable, Authentication authentication);
    Page<DeliverySummaryResponse> getCookDeliveries(Pageable pageable, Authentication authentication);
    Delivery getDeliveryOrThrow(Long deliveryId);
}
