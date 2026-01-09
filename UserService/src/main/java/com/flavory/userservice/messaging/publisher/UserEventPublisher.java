package com.flavory.userservice.messaging.publisher;

import com.flavory.userservice.config.RabbitMQConfig;
import com.flavory.userservice.entity.User;
import com.flavory.userservice.event.outbound.UserUpdatedEvent;
import com.flavory.userservice.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final UserMapper userMapper;

    public void publishUserUpdated(User user) {
        UserUpdatedEvent event = userMapper.toUserUpdatedEvent(user);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_EXCHANGE,
                    RabbitMQConfig.USER_UPDATED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish UserUpdatedEvent", e);
        }
    }
}
