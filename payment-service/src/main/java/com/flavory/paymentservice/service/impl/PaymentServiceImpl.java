package com.flavory.paymentservice.service.impl;

import com.flavory.paymentservice.dto.request.CreatePaymentIntentRequest;
import com.flavory.paymentservice.dto.request.RefundRequest;
import com.flavory.paymentservice.dto.response.PaymentIntentResponse;
import com.flavory.paymentservice.dto.response.PaymentResponse;
import com.flavory.paymentservice.dto.response.RefundResponse;
import com.flavory.paymentservice.entity.Payment;
import com.flavory.paymentservice.entity.PaymentStatus;
import com.flavory.paymentservice.exception.*;
import com.flavory.paymentservice.mapper.PaymentMapper;
import com.flavory.paymentservice.messaging.publisher.PaymentEventPublisher;
import com.flavory.paymentservice.repository.PaymentRepository;
import com.flavory.paymentservice.service.PaymentService;
import com.flavory.paymentservice.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;
    private final PaymentEventPublisher eventPublisher;

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

            eventPublisher.publishPaymentCreated(payment);

            return paymentMapper.toPaymentIntentResponse(payment);

        } catch (StripeIntegrationException e) {
            throw new PaymentProcessingException("Nie udało się przetworzyć płatności", e);
        }
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentIntentId));

        PaymentIntent paymentIntent = stripeService.retrievePaymentIntent(paymentIntentId);

        switch (paymentIntent.getStatus()) {
            case "succeeded":
                handlePaymentSuccess(payment, paymentIntent);
                break;
            case "requires_action":
                payment.updateStatus(PaymentStatus.REQUIRES_ACTION);
                payment = paymentRepository.save(payment);
                break;
            case "processing":
                payment.updateStatus(PaymentStatus.PROCESSING);
                payment = paymentRepository.save(payment);
                break;
            default:
        }

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (!payment.canBeCancelled()) {
            throw new InvalidPaymentStateException(
                    paymentId,
                    payment.getStatus(),
                    "anulowanie"
            );
        }

        stripeService.cancelPaymentIntent(payment.getStripePaymentIntentId());

        payment.markAsCancelled();
        payment = paymentRepository.save(payment);

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getCustomerPayments(String customerId, Pageable pageable) {
        return paymentRepository.findByCustomerId(customerId, pageable)
                .map(paymentMapper::toPaymentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getCookPayments(String cookId, Pageable pageable) {
        return paymentRepository.findByCookId(cookId, pageable)
                .map(paymentMapper::toPaymentResponse);
    }

    @Override
    @Transactional
    public RefundResponse refundPayment(Long paymentId, RefundRequest refundRequest) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (!payment.canBeRefunded()) {
            throw new RefundNotAllowedException(paymentId, payment.getStatus());
        }

        if (refundRequest.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new RefundNotAllowedException(
                    "Kwota zwrotu nie może przekraczać kwoty płatności"
            );
        }

        Refund refund = stripeService.createRefund(
                payment.getStripePaymentIntentId(),
                refundRequest
        );

        payment.markAsRefunded(
                refund.getId(),
                refundRequest.getAmount(),
                refundRequest.getReason()
        );
        payment = paymentRepository.save(payment);

        return RefundResponse.builder()
                .paymentId(payment.getId())
                .refundId(refund.getId())
                .refundAmount(refundRequest.getAmount())
                .refundReason(refundRequest.getReason())
                .status(payment.getStatus())
                .refundedAt(payment.getRefundedAt())
                .build();
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

    private void handlePaymentSuccess(Payment payment, PaymentIntent paymentIntent) {
        payment.markAsPaid(
                paymentIntent.getLatestCharge(),
                LocalDateTime.now()
        );

        String chargeId = paymentIntent.getLatestCharge();

        if (chargeId != null) {
            try {
                Charge charge = Charge.retrieve(chargeId);

                if (charge.getPaymentMethodDetails() != null &&
                        charge.getPaymentMethodDetails().getCard() != null) {

                    payment.setCardLast4(charge.getPaymentMethodDetails().getCard().getLast4());
                    payment.setCardBrand(charge.getPaymentMethodDetails().getCard().getBrand());
                }
            } catch (StripeException ignored) {
            }
        }

        payment = paymentRepository.save(payment);
    }
}
