package com.flavory.paymentservice.messaging.publisher;

import com.flavory.paymentservice.config.RabbitMQConfig;
import com.flavory.paymentservice.entity.Payment;
import com.flavory.paymentservice.event.PaymentEvent;
import com.flavory.paymentservice.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final PaymentMapper paymentMapper;

    public void publishPaymentCreated(Payment payment) {
        PaymentEvent event = paymentMapper.toPaymentCreatedEvent(payment);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    "payment.created",
                    event
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish PaymentCreatedEvent", e);
        }
    }

    public void publishPaymentSucceeded(Payment payment) {
        PaymentEvent event = paymentMapper.toPaymentSucceededEvent(payment);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_SUCCEEDED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish PaymentSucceededEvent", e);
        }
    }

}
