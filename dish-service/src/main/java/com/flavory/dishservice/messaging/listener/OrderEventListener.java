package com.flavory.dishservice.messaging.listener;

import com.flavory.dishservice.config.RabbitMQConfig;
import com.flavory.dishservice.entity.ProcessedEventEntity;
import com.flavory.dishservice.event.inbound.OrderPlacedEvent;
import com.flavory.dishservice.repository.ProcessedEventRepository;
import com.flavory.dishservice.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final DishService dishService;
    private final ProcessedEventRepository processedEventRepository;

    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (isEventProcessed(event.getEventId())) {
            return;
        }

        for (OrderPlacedEvent.OrderItem item : event.getItems()) {
            try {
                dishService.decreaseStock(item.getDishId(), item.getQuantity());

            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to decrease stock for dish " + item.getDishId(), e);
            }
        }
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
