package com.flavory.paymentservice.mapper;

import com.flavory.paymentservice.dto.response.PaymentIntentResponse;
import com.flavory.paymentservice.dto.response.PaymentResponse;
import com.flavory.paymentservice.entity.Payment;
import com.flavory.paymentservice.event.PaymentEvent;
import com.flavory.paymentservice.event.PaymentEventType;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentMapper {

    @Mapping(target = "paymentId", source = "id")
    PaymentIntentResponse toPaymentIntentResponse(Payment payment);

    PaymentResponse toPaymentResponse(Payment payment);

    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    PaymentEvent toPaymentEvent(Payment payment, PaymentEventType eventType);

    default PaymentEvent toPaymentCreatedEvent(Payment payment) {
        return toPaymentEvent(payment, PaymentEventType.PAYMENT_CREATED);
    }

    default PaymentEvent toPaymentSucceededEvent(Payment payment) {
        return toPaymentEvent(payment, PaymentEventType.PAYMENT_SUCCEEDED);
    }
}
