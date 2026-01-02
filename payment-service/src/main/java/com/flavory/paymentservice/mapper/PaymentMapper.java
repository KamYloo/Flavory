package com.flavory.paymentservice.mapper;

import com.flavory.paymentservice.dto.response.PaymentIntentResponse;
import com.flavory.paymentservice.dto.response.PaymentResponse;
import com.flavory.paymentservice.entity.Payment;
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
}
