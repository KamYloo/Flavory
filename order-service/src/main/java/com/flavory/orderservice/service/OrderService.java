package com.flavory.orderservice.service;

import com.flavory.orderservice.dto.request.CreateOrderRequest;
import com.flavory.orderservice.dto.response.OrderResponse;
import com.flavory.orderservice.dto.response.OrderSummaryResponse;
import com.flavory.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;


public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, Authentication authentication);
    OrderResponse getOrderById(Long orderId, Authentication authentication);
    Page<OrderSummaryResponse> getCustomerOrders(Pageable pageable, Authentication authentication);
    Order getOrderOrThrow(Long orderId);
    void validateOrderAccess(Order order, String userId);
}
