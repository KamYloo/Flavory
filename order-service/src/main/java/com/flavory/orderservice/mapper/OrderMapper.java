package com.flavory.orderservice.mapper;

import com.flavory.orderservice.dto.response.OrderResponse;
import com.flavory.orderservice.entity.Order;
import org.mapstruct.*;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "orderStatusToString")
    @Mapping(source = "status", target = "statusDisplayName", qualifiedByName = "orderStatusToDisplayName")
    @Mapping(source = "items", target = "items")
    @Mapping(source = "deliveryAddress", target = "deliveryAddress")
    OrderResponse toResponse(Order order);


    @Named("orderStatusToString")
    default String orderStatusToString(Order.OrderStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("orderStatusToDisplayName")
    default String orderStatusToDisplayName(Order.OrderStatus status) {
        return status != null ? status.getDisplayName() : null;
    }
}
