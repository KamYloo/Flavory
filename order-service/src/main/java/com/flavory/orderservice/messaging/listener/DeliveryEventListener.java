package com.flavory.orderservice.messaging.listener;

import com.flavory.orderservice.config.RabbitMQConfig;
import com.flavory.orderservice.entity.Order;
import com.flavory.orderservice.entity.ProcessedEventEntity;
import com.flavory.orderservice.event.inbound.DeliveryStartedEvent;
import com.flavory.orderservice.exception.OrderNotFoundException;
import com.flavory.orderservice.repository.OrderRepository;
import com.flavory.orderservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeliveryEventListener {
    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.DELIVERY_STARTED_QUEUE)
    public void handleDeliveryStarted(DeliveryStartedEvent event) {
        if (isEventProcessed(event.getEventId())) {
            return;
        }

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));

        order.updateStatus(Order.OrderStatus.IN_DELIVERY);

        if (event.getTrackingUrl() != null) {
            order.setGlovoTrackingUrl(event.getTrackingUrl());
        }
        orderRepository.save(order);
        markEventAsProcessed(event.getEventId());
    }

    private boolean isEventProcessed(String eventId) {
        if (eventId == null) {
            return false;
        }
        return processedEventRepository.existsByEventId(eventId);
    }

    private void markEventAsProcessed(String eventId) {
        if (eventId == null) {
            return;
        }
        try {
            ProcessedEventEntity entity = new ProcessedEventEntity(eventId);
            processedEventRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark event as processed", e);
        }
    }
}
