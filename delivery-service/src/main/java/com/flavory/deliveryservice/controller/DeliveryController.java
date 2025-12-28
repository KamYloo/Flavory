package com.flavory.deliveryservice.controller;

import com.flavory.deliveryservice.dto.response.DeliveryResponse;
import com.flavory.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(
            @PathVariable Long orderId,
            Authentication authentication) {

        DeliveryResponse response = deliveryService.getDeliveryByOrderId(orderId, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(
            @PathVariable Long deliveryId,
            Authentication authentication) {

        DeliveryResponse response = deliveryService.getDeliveryById(deliveryId, authentication);
        return ResponseEntity.ok(response);
    }
}
