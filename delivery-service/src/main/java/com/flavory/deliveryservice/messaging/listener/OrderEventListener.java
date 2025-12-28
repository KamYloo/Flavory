package com.flavory.deliveryservice.messaging.listener;

import com.flavory.deliveryservice.config.RabbitMQConfig;
import com.flavory.deliveryservice.entity.ProcessedEventEntity;
import com.flavory.deliveryservice.event.inbound.OrderReadyEvent;
import com.flavory.deliveryservice.repository.ProcessedEventRepository;
import com.flavory.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final DeliveryService deliveryService;
    private final ProcessedEventRepository processedEventRepository;

    @RabbitListener(queues = RabbitMQConfig.ORDER_READY_QUEUE)
    @Transactional
    public void handleOrderReady(OrderReadyEvent event) {
        if (isEventProcessed(event.getEventId())) {
            return;
        }
        deliveryService.createDeliveryFromEvent(event);
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
