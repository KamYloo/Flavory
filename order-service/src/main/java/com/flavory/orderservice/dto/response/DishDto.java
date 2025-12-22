package com.flavory.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishDto {
    private Long id;
    private String cookId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer preparationTime;
    private Boolean available;
    private Integer currentStock;
    private List<String> images;
}