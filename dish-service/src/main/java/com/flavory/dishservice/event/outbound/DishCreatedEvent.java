package com.flavory.dishservice.event.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishCreatedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long dishId;
    private String cookId;
    private String dishName;
    private BigDecimal price;
    private String category;
    private Boolean available;
    private Integer currentStock;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String eventId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime eventTimestamp = LocalDateTime.now();
}
