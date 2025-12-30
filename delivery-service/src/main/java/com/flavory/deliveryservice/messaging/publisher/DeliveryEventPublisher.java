package com.flavory.deliveryservice.messaging.publisher;

import com.flavory.deliveryservice.config.RabbitMQConfig;
import com.flavory.deliveryservice.event.outbound.DeliveryPickedUpEvent;
import com.flavory.deliveryservice.event.outbound.DeliveryStartedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishDeliveryStarted(DeliveryStartedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DELIVERY_EXCHANGE,
                    RabbitMQConfig.DELIVERY_STARTED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish DeliveryStartedEvent", e);
        }
    }

    public void publishDeliveryPickedUp(DeliveryPickedUpEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DELIVERY_EXCHANGE,
                    RabbitMQConfig.DELIVERY_PICKED_UP_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish DeliveryPickedUpEvent", e);
        }
    }

}
