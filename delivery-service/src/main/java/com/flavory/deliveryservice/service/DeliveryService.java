package com.flavory.deliveryservice.service;


import com.flavory.deliveryservice.event.inbound.OrderReadyEvent;

public interface  DeliveryService {
    void createDeliveryFromEvent(OrderReadyEvent event);
}
