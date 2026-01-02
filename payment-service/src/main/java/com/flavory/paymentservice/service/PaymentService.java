package com.flavory.paymentservice.service;

import com.flavory.paymentservice.dto.request.CreatePaymentIntentRequest;
import com.flavory.paymentservice.dto.response.PaymentIntentResponse;
import com.flavory.paymentservice.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request);
    PaymentResponse confirmPayment(String paymentIntentId);
    PaymentResponse cancelPayment(Long paymentId);
    PaymentResponse getPaymentById(Long paymentId);
    PaymentResponse getPaymentByOrderId(Long orderId);
    Page<PaymentResponse> getCustomerPayments(String customerId, Pageable pageable);
}
