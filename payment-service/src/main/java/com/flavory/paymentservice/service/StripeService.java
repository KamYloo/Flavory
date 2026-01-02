package com.flavory.paymentservice.service;

import com.flavory.paymentservice.entity.PaymentMethod;
import com.stripe.model.PaymentIntent;

import java.math.BigDecimal;

public interface StripeService {
    PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String customerId, Long orderId, PaymentMethod paymentMethod);
    PaymentIntent cancelPaymentIntent(String paymentIntentId);
    PaymentIntent retrievePaymentIntent(String paymentIntentId);
}
