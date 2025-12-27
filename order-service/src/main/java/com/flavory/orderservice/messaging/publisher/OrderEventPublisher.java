package com.flavory.orderservice.messaging.publisher;

import com.flavory.orderservice.config.RabbitMQConfig;
import com.flavory.orderservice.event.outbound.OrderCompletedEvent;
import com.flavory.orderservice.event.outbound.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_PLACED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish OrderPlacedEvent", e);
        }
    }

    public void publishOrderCompleted(OrderCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_COMPLETED_ROUTING_KEY,
                    event
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish OrderCompletedEvent", e);
        }
    }
}
