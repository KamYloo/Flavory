package com.flavory.dishservice.utils;

import com.flavory.dishservice.entity.ProcessedEventEntity;
import com.flavory.dishservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProcessedUtil {
    private final ProcessedEventRepository processedEventRepository;

    public boolean isEventProcessed(String eventId) {
        if (eventId == null) {
            return false;
        }
        return processedEventRepository.existsByEventId(eventId);
    }

    public void markEventAsProcessed(String eventId) {
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
