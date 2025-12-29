package com.flavory.deliveryservice.controller;

import com.flavory.deliveryservice.dto.response.DeliveryResponse;
import com.flavory.deliveryservice.dto.response.DeliverySummaryResponse;
import com.flavory.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/customer/me")
    public ResponseEntity<Page<DeliverySummaryResponse>> getMyDeliveries(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication) {

        Page<DeliverySummaryResponse> response = deliveryService.getCustomerDeliveries(
                pageable, authentication);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/cook/me")
    public ResponseEntity<Page<DeliverySummaryResponse>> getMyCookDeliveries(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication) {

        Page<DeliverySummaryResponse> response = deliveryService.getCookDeliveries(
                pageable, authentication);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deliveryId}/cancel")
    public ResponseEntity<DeliveryResponse> cancelDelivery(
            @PathVariable Long deliveryId,
            @RequestParam String reason,
            Authentication authentication) {

        DeliveryResponse response = deliveryService.cancelDelivery(
                deliveryId, reason, authentication);

        return ResponseEntity.ok(response);
    }
}
