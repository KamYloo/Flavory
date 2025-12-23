package com.flavory.orderservice.controller;

import com.flavory.orderservice.dto.request.CancelOrderRequest;
import com.flavory.orderservice.dto.request.CreateOrderRequest;
import com.flavory.orderservice.dto.request.UpdateOrderStatusRequest;
import com.flavory.orderservice.dto.response.OrderResponse;
import com.flavory.orderservice.dto.response.OrderSummaryResponse;
import com.flavory.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        OrderResponse response = orderService.createOrder(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        OrderResponse response = orderService.getOrderById(orderId, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/me")
    public ResponseEntity<Page<OrderSummaryResponse>> getMyOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication) {

        Page<OrderSummaryResponse> response = orderService.getCustomerOrders(pageable, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cook/me")
    public ResponseEntity<Page<OrderSummaryResponse>> getMyCookOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication) {

        Page<OrderSummaryResponse> response = orderService.getCookOrders(pageable, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cook/me/status/{status}")
    public ResponseEntity<Page<OrderSummaryResponse>> getMyCookOrdersByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication) {

        Page<OrderSummaryResponse> response = orderService.getCookOrdersByStatus(
                status, pageable, authentication);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {

        OrderResponse response = orderService.updateOrderStatus(orderId, request, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request,
            Authentication authentication) {

        OrderResponse response = orderService.cancelOrder(orderId, request, authentication);
        return ResponseEntity.ok(response);
    }
}
