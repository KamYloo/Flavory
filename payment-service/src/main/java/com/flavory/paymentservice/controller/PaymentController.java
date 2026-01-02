package com.flavory.paymentservice.controller;

import com.flavory.paymentservice.dto.request.ConfirmPaymentRequest;
import com.flavory.paymentservice.dto.request.CreatePaymentIntentRequest;
import com.flavory.paymentservice.dto.response.PaymentIntentResponse;
import com.flavory.paymentservice.dto.response.PaymentResponse;
import com.flavory.paymentservice.security.JwtService;
import com.flavory.paymentservice.service.PaymentService;
import com.flavory.paymentservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final JwtService jwtService;
    private final PaymentService paymentService;

    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            Authentication authentication) {

        String userId = jwtService.extractAuth0Id(authentication);

        if (!request.getCustomerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PaymentIntentResponse response = paymentService.createPaymentIntent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest request
    ) {

        PaymentResponse response = paymentService.confirmPayment(request.getPaymentIntentId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long id,
            Authentication authentication) {
        String userId = jwtService.extractAuth0Id(authentication);

        PaymentResponse payment = paymentService.getPaymentById(id);

        if (!payment.getCustomerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PaymentResponse cancelledPayment = paymentService.cancelPayment(id);
        return ResponseEntity.ok(cancelledPayment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id,
            Authentication authentication) {

        String userId = jwtService.extractAuth0Id(authentication);
        PaymentResponse payment = paymentService.getPaymentById(id);

        if (!SecurityUtils.hasAccessToPayment(payment, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            Authentication authentication) {

        String userId = jwtService.extractAuth0Id(authentication);
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);

        if (!SecurityUtils.hasAccessToPayment(payment, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(payment);
    }
}