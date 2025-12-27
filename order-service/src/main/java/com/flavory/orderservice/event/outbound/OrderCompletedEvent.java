package com.flavory.orderservice.event.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String customerId;
    private String cookId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private BigDecimal dishRating;
    private Long ratedDishId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    private String eventId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long dishId;
        private String dishName;
        private Integer quantity;
        private BigDecimal itemTotal;
        private BigDecimal unitPrice;
    }
}
