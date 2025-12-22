package com.flavory.orderservice.validator;

import com.flavory.orderservice.dto.request.OrderItemRequest;
import com.flavory.orderservice.dto.response.DishDto;
import com.flavory.orderservice.exception.DishNotAvailableException;
import com.flavory.orderservice.exception.InsufficientStockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderValidator {

    @Value("${app.business.max-items-per-order}")
    private Integer maxItemsPerOrder;

    @Value("${app.business.min-order-amount}")
    private BigDecimal minOrderAmount;

    @Value("${app.business.max-order-amount}")
    private BigDecimal maxOrderAmount;

    public void validateOrderCreation(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        if (items.size() > maxItemsPerOrder) {
            throw new IllegalArgumentException(
                    "Order cannot contain more than " + maxItemsPerOrder + " items");
        }
    }

    public void validateDishAvailability(DishDto dish, Integer requestedQuantity) {
        if (dish == null) {
            throw new DishNotAvailableException("Dish not found");
        }

        if (!Boolean.TRUE.equals(dish.getAvailable())) {
            throw new DishNotAvailableException(dish.getName());
        }

        if (dish.getCurrentStock() == null || dish.getCurrentStock() < requestedQuantity) {
            throw new InsufficientStockException(
                    dish.getName(),
                    dish.getCurrentStock() != null ? dish.getCurrentStock() : 0,
                    requestedQuantity
            );
        }
    }

    public void validateOrderAmount(BigDecimal totalAmount) {
        if (totalAmount.compareTo(minOrderAmount) < 0) {
            throw new IllegalArgumentException(
                    "Order amount must be at least " + minOrderAmount + " PLN");
        }

        if (totalAmount.compareTo(maxOrderAmount) > 0) {
            throw new IllegalArgumentException(
                    "Order amount cannot exceed " + maxOrderAmount + " PLN");
        }
    }
}
