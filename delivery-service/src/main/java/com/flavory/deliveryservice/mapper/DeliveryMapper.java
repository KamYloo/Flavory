package com.flavory.deliveryservice.mapper;

import com.flavory.deliveryservice.dto.response.DeliveryResponse;
import com.flavory.deliveryservice.entity.Delivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliveryMapper {
    @Mapping(source = "status", target = "status", qualifiedByName = "deliveryStatusToString")
    @Mapping(source = "status", target = "statusDisplayName", qualifiedByName = "deliveryStatusToDisplayName")
    DeliveryResponse toResponse(Delivery delivery);

    @Named("deliveryStatusToString")
    default String deliveryStatusToString(Delivery.DeliveryStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("deliveryStatusToDisplayName")
    default String deliveryStatusToDisplayName(Delivery.DeliveryStatus status) {
        return status != null ? status.getDisplayName() : null;
    }
}
