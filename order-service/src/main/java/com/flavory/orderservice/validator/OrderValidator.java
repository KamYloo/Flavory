package com.flavory.orderservice.validator;

import com.flavory.orderservice.dto.request.OrderItemRequest;
import com.flavory.orderservice.dto.response.DishDto;
import com.flavory.orderservice.entity.Order;
import com.flavory.orderservice.exception.DishNotAvailableException;
import com.flavory.orderservice.exception.InsufficientStockException;
import com.flavory.orderservice.exception.InvalidOrderStatusException;
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
            throw new IllegalArgumentException("Zamówienie musi zawierać co najmniej jedną pozycję");
        }

        if (items.size() > maxItemsPerOrder) {
            throw new IllegalArgumentException(
                    "Zamówienie nie może zawierać więcej niż " + maxItemsPerOrder + " pozycji");
        }
    }

    public void validateDishAvailability(DishDto dish, Integer requestedQuantity) {
        if (dish == null) {
            throw new DishNotAvailableException("Danie nie zostało znalezione");
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
                    "Kwota zamówienia musi wynosić co najmniej " + minOrderAmount + " PLN");
        }

        if (totalAmount.compareTo(maxOrderAmount) > 0) {
            throw new IllegalArgumentException(
                    "Kwota zamówienia nie może przekroczyć " + maxOrderAmount + " PLN");
        }
    }

    public void validateStatusTransition(Order.OrderStatus from, Order.OrderStatus to) {
        boolean isValid = switch (from) {
            case PENDING -> to == Order.OrderStatus.PAID || to == Order.OrderStatus.CANCELLED;
            case PAID -> to == Order.OrderStatus.CONFIRMED || to == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> to == Order.OrderStatus.PREPARING || to == Order.OrderStatus.CANCELLED;
            case PREPARING -> to == Order.OrderStatus.READY;
            case READY -> to == Order.OrderStatus.IN_DELIVERY;
            case IN_DELIVERY -> to == Order.OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED, FAILED -> false;
        };

        if (!isValid) {
            throw new InvalidOrderStatusException(from, to);
        }
    }
}
