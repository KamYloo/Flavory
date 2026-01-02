package com.flavory.paymentservice.service;

import com.flavory.paymentservice.dto.request.RefundRequest;
import com.flavory.paymentservice.entity.PaymentMethod;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;

import java.math.BigDecimal;

public interface StripeService {
    PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String customerId, Long orderId, PaymentMethod paymentMethod);
    PaymentIntent cancelPaymentIntent(String paymentIntentId);
    PaymentIntent retrievePaymentIntent(String paymentIntentId);
    Refund createRefund(String paymentIntentId, RefundRequest refundRequest);
}
