package com.flavory.dishservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEventEntity {

    @Id
    private String eventId;

    private LocalDateTime processedAt;

    public ProcessedEventEntity(String eventId) {
        this.eventId = eventId;
        this.processedAt = LocalDateTime.now();
    }
}
