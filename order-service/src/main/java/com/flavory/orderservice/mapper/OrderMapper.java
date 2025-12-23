package com.flavory.orderservice.mapper;

import com.flavory.orderservice.dto.response.OrderResponse;
import com.flavory.orderservice.dto.response.OrderSummaryResponse;
import com.flavory.orderservice.entity.Order;
import com.flavory.orderservice.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "orderStatusToString")
    @Mapping(source = "status", target = "statusDisplayName", qualifiedByName = "orderStatusToDisplayName")
    @Mapping(source = "items", target = "items")
    @Mapping(source = "deliveryAddress", target = "deliveryAddress")
    OrderResponse toResponse(Order order);

    @Mapping(source = "status", target = "status", qualifiedByName = "orderStatusToString")
    @Mapping(source = "status", target = "statusDisplayName", qualifiedByName = "orderStatusToDisplayName")
    @Mapping(source = "items", target = "itemsCount", qualifiedByName = "countItems")
    OrderSummaryResponse toSummaryResponse(Order order);


    @Named("orderStatusToString")
    default String orderStatusToString(Order.OrderStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("orderStatusToDisplayName")
    default String orderStatusToDisplayName(Order.OrderStatus status) {
        return status != null ? status.getDisplayName() : null;
    }

    @Named("countItems")
    default Integer countItems(List<OrderItem> items) {
        return items != null ? items.size() : 0;
    }
}
