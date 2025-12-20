package com.flavory.dishservice.messaging.publisher;
import com.flavory.dishservice.config.RabbitMQConfig;
import com.flavory.dishservice.event.outbound.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DishEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishDishCreated(DishCreatedEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DISH_EXCHANGE,
                RabbitMQConfig.DISH_CREATED_ROUTING_KEY,
                event
        );
    }

    public void publishDishUpdated(DishUpdatedEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DISH_EXCHANGE,
                RabbitMQConfig.DISH_UPDATED_ROUTING_KEY,
                event
        );
    }

    public void publishDishDeleted(DishDeletedEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DISH_EXCHANGE,
                RabbitMQConfig.DISH_DELETED_ROUTING_KEY,
                event
        );
    }

    public void publishDishAvailabilityChanged(DishAvailabilityChangedEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DISH_EXCHANGE,
                RabbitMQConfig.DISH_AVAILABILITY_CHANGED_ROUTING_KEY,
                event
        );
    }
}
