package com.flavory.orderservice.service;

import com.flavory.orderservice.dto.request.CreateOrderRequest;
import com.flavory.orderservice.dto.response.OrderResponse;
import com.flavory.orderservice.entity.Order;
import org.springframework.security.core.Authentication;


public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, Authentication authentication);
    OrderResponse getOrderById(Long orderId, Authentication authentication);
    Order getOrderOrThrow(Long orderId);
    void validateOrderAccess(Order order, String userId);
}
