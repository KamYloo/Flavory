package com.flavory.dishservice.messaging.listener;

import com.flavory.dishservice.config.RabbitMQConfig;
import com.flavory.dishservice.entity.CookProfile;
import com.flavory.dishservice.event.inbound.UserUpdatedEvent;
import com.flavory.dishservice.repository.CookProfileRepository;
import com.flavory.dishservice.utils.EventProcessedUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final CookProfileRepository cookProfileRepository;
    private final EventProcessedUtil eventProcessedUtil;


    @Transactional
    @RabbitListener(queues = RabbitMQConfig.USER_UPDATED_QUEUE)
    public void handleUserUpdated(UserUpdatedEvent event) {
        if (eventProcessedUtil.isEventProcessed(event.getEventId())) {
            return;
        }

        CookProfile profile = cookProfileRepository.findById(event.getAuth0Id())
                .orElse(CookProfile.builder().cookId(event.getAuth0Id()).build());

        profile.setFirstName(event.getFirstName());
        profile.setLastName(event.getLastName());
        profile.setProfileImageUrl(event.getProfileImageUrl());
        profile.setRole(event.getRole());
        profile.setStatus(event.getStatus());
        cookProfileRepository.save(profile);

        eventProcessedUtil.markEventAsProcessed(event.getEventId());
    }
}
