package com.flavory.paymentservice.service;

import com.flavory.paymentservice.dto.request.CreatePaymentIntentRequest;
import com.flavory.paymentservice.dto.response.PaymentIntentResponse;
import com.flavory.paymentservice.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request);
    PaymentResponse confirmPayment(String paymentIntentId);
    PaymentResponse cancelPayment(Long paymentId);
    PaymentResponse getPaymentById(Long paymentId);
}
