package com.flavory.paymentservice.service.impl;

import com.flavory.paymentservice.dto.request.CreatePaymentIntentRequest;
import com.flavory.paymentservice.dto.response.PaymentIntentResponse;
import com.flavory.paymentservice.entity.Payment;
import com.flavory.paymentservice.entity.PaymentStatus;
import com.flavory.paymentservice.exception.DuplicatePaymentException;
import com.flavory.paymentservice.exception.InvalidPaymentAmountException;
import com.flavory.paymentservice.exception.PaymentProcessingException;
import com.flavory.paymentservice.exception.StripeIntegrationException;
import com.flavory.paymentservice.mapper.PaymentMapper;
import com.flavory.paymentservice.repository.PaymentRepository;
import com.flavory.paymentservice.service.PaymentService;
import com.flavory.paymentservice.service.StripeService;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;

    private static final BigDecimal MIN_PAYMENT_AMOUNT = new BigDecimal("1.00");
    private static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("10000.00");

    @Transactional
    @Override
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        validatePaymentAmount(request.getAmount());
        checkForDuplicatePayment(request.getOrderId());

        try {
            BigDecimal platformFee = stripeService.calculatePlatformFee(request.getAmount());
            BigDecimal cookPayout = stripeService.calculateCookPayout(request.getAmount(), platformFee);

            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                    request.getAmount(),
                    "pln",
                    request.getCustomerId(),
                    request.getOrderId(),
                    request.getPaymentMethod()
            );

            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .customerId(request.getCustomerId())
                    .cookId(request.getCookId())
                    .amount(request.getAmount())
                    .currency("PLN")
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(request.getPaymentMethod())
                    .stripePaymentIntentId(paymentIntent.getId())
                    .stripeClientSecret(paymentIntent.getClientSecret())
                    .platformFee(platformFee)
                    .cookPayout(cookPayout)
                    .metadata(request.getMetadata())
                    .build();

            payment = paymentRepository.save(payment);

            return paymentMapper.toPaymentIntentResponse(payment);

        } catch (StripeIntegrationException e) {
            throw new PaymentProcessingException("Nie udało się przetworzyć płatności", e);
        }
    }

    private void validatePaymentAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(MIN_PAYMENT_AMOUNT) < 0) {
            throw new InvalidPaymentAmountException(amount);
        }
        if (amount.compareTo(MAX_PAYMENT_AMOUNT) > 0) {
            throw new PaymentProcessingException(
                    String.format("Kwota płatności nie może przekraczać %s PLN", MAX_PAYMENT_AMOUNT)
            );
        }
    }

    private void checkForDuplicatePayment(Long orderId) {
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new DuplicatePaymentException(orderId);
        }
    }
}
