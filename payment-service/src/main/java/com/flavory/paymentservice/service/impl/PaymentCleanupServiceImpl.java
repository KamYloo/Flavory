package com.flavory.paymentservice.service.impl;

import com.flavory.paymentservice.entity.Payment;
import com.flavory.paymentservice.messaging.publisher.PaymentEventPublisher;
import com.flavory.paymentservice.repository.PaymentRepository;
import com.flavory.paymentservice.service.PaymentCleanupService;
import com.flavory.paymentservice.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCleanupServiceImpl implements PaymentCleanupService {
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final PaymentEventPublisher eventPublisher;

    @Override
    @Transactional
    @Scheduled(cron = "0 */10 * * * *")
    public void cancelExpiredPendingPayments() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(30);
        List<Payment> expiredPayments = paymentRepository.findExpiredPendingPayments(expiryTime);

        if (expiredPayments.isEmpty()) {
            return;
        }

        for (Payment payment : expiredPayments) {
            stripeService.cancelPaymentIntent(payment.getStripePaymentIntentId());

            payment.markAsCancelled();
            paymentRepository.save(payment);

            eventPublisher.publishPaymentCancelled(payment);
        }
    }
}
