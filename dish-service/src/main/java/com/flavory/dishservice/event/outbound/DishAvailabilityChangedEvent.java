package com.flavory.dishservice.event.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishAvailabilityChangedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long dishId;
    private String cookId;
    private Boolean available;
    private Integer currentStock;
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime changedAt;

    private String eventId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime eventTimestamp = LocalDateTime.now();
}
