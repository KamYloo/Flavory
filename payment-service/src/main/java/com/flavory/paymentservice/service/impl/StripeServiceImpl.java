package com.flavory.paymentservice.service.impl;

import com.flavory.paymentservice.config.PaymentProperties;
import com.flavory.paymentservice.entity.PaymentMethod;
import com.flavory.paymentservice.exception.StripeIntegrationException;
import com.flavory.paymentservice.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    private final PaymentProperties paymentProperties;

    @Override
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String customerId, Long orderId, PaymentMethod paymentMethod) {
        try {
            Long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("orderId", orderId.toString());
            metadata.put("customerId", customerId);
            metadata.put("paymentMethod", paymentMethod.name());

            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    );

            return PaymentIntent.create(paramsBuilder.build());

        } catch (StripeException e) {
            throw new StripeIntegrationException(
                    "Nie udało się utworzyć Payment Intent: " + e.getUserMessage(), e
            );
        }
    }
}
