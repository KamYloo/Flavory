package com.flavory.orderservice.service;

import com.flavory.orderservice.dto.request.CreateOrderRequest;
import com.flavory.orderservice.dto.response.OrderResponse;
import org.springframework.security.core.Authentication;


public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, Authentication authentication);
}
