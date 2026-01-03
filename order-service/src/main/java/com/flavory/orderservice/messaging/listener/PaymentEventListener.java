package com.flavory.orderservice.messaging.listener;

import com.flavory.orderservice.config.RabbitMQConfig;
import com.flavory.orderservice.entity.Order;
import com.flavory.orderservice.entity.ProcessedEventEntity;
import com.flavory.orderservice.event.inbound.PaymentSucceededEvent;
import com.flavory.orderservice.exception.OrderNotFoundException;
import com.flavory.orderservice.repository.OrderRepository;
import com.flavory.orderservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCEEDED_QUEUE)
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {

        if (isEventProcessed(event.getEventId())) {
            return;
        }

        try {
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));

            if (order.getStatus() != Order.OrderStatus.PENDING) {
                markEventAsProcessed(event.getEventId());
                return;
            }

            order.updateStatus(Order.OrderStatus.PAID);

            if (event.getStripePaymentIntentId() != null) {
                order.setPaymentTransactionId(event.getStripePaymentIntentId());
            }

            orderRepository.save(order);
            markEventAsProcessed(event.getEventId());

        } catch (OrderNotFoundException e) {
            markEventAsProcessed(event.getEventId());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process PaymentSucceededEvent", e);
        }
    }

    private boolean isEventProcessed(String eventId) {
        if (eventId == null) {
            return false;
        }
        return processedEventRepository.existsByEventId(eventId);
    }

    private void markEventAsProcessed(String eventId) {
        if (eventId == null) {
            return;
        }
        try {
            ProcessedEventEntity entity = new ProcessedEventEntity(eventId);
            processedEventRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark event as processed", e);
        }
    }
}