package com.flavory.deliveryservice.event.outbound;

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
public class DeliveryPickedUpEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long deliveryId;
    private Long orderId;
    private String courierName;
    private String courierPhone;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pickedUpAt;

    private String eventId;
}
