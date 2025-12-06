package com.flavory.dishservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStockRequest {

    @NotNull(message = "Bieżący stan jest wymagany")
    @Min(value = 0, message = "Stan nie może być ujemny")
    @Max(value = 100, message = "Stan nie może przekroczyć 100")
    private Integer currentStock;

    @Min(value = 0, message = "Maksymalny dzienny stan nie może być ujemny")
    @Max(value = 100, message = "Maksymalny dzienny stan nie może przekroczyć 100")
    private Integer maxDailyStock;
}