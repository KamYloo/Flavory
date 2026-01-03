package com.flavory.paymentservice.scheduler;

import com.flavory.paymentservice.entity.Payment;
import com.flavory.paymentservice.entity.PaymentStatus;
import com.flavory.paymentservice.messaging.publisher.PaymentEventPublisher;
import com.flavory.paymentservice.repository.PaymentRepository;
import com.flavory.paymentservice.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCleanupJob {
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final PaymentEventPublisher eventPublisher;

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

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void synchronizePaymentStatuses() {

        List<Payment> processingPayments = paymentRepository.findByStatus(PaymentStatus.PROCESSING);

        for (Payment payment : processingPayments) {
            var paymentIntent = stripeService.retrievePaymentIntent(
                    payment.getStripePaymentIntentId()
            );

            boolean statusChanged = false;

            switch (paymentIntent.getStatus()) {
                case "succeeded":
                    payment.markAsPaid(
                            paymentIntent.getLatestCharge(),
                            LocalDateTime.now()
                    );
                    eventPublisher.publishPaymentSucceeded(payment);
                    statusChanged = true;
                    break;

                case "canceled":
                    payment.markAsCancelled();
                    eventPublisher.publishPaymentCancelled(payment);
                    statusChanged = true;
                    break;

                case "requires_payment_method":
                    payment.markAsFailed(
                            "requires_payment_method",
                            "Płatność wymaga metody płatności"
                    );
                    eventPublisher.publishPaymentFailed(payment);
                    statusChanged = true;
                    break;
            }

            if (statusChanged) {
                paymentRepository.save(payment);
            }

        }

    }
}
