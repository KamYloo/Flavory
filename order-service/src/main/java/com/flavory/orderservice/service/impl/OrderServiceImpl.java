package com.flavory.orderservice.service.impl;

import com.flavory.orderservice.client.DishServiceClient;
import com.flavory.orderservice.client.UserServiceClient;
import com.flavory.orderservice.dto.request.*;
import com.flavory.orderservice.dto.response.AddressDto;
import com.flavory.orderservice.dto.response.DishDto;
import com.flavory.orderservice.dto.response.OrderResponse;
import com.flavory.orderservice.dto.response.OrderSummaryResponse;
import com.flavory.orderservice.entity.DeliveryAddress;
import com.flavory.orderservice.entity.Order;
import com.flavory.orderservice.entity.OrderItem;
import com.flavory.orderservice.exception.AddressNotFoundException;
import com.flavory.orderservice.exception.DishNotAvailableException;
import com.flavory.orderservice.exception.OrderNotFoundException;
import com.flavory.orderservice.exception.UnauthorizedOrderAccessException;
import com.flavory.orderservice.mapper.OrderMapper;
import com.flavory.orderservice.repository.OrderRepository;
import com.flavory.orderservice.security.JwtService;
import com.flavory.orderservice.service.OrderService;
import com.flavory.orderservice.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final JwtService jwtService;
    private final OrderValidator orderValidator;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final DishServiceClient dishServiceClient;

    @Value("${app.business.delivery-fee}")
    private BigDecimal deliveryFee;

    @Value("${app.business.free-delivery-threshold}")
    private BigDecimal freeDeliveryThreshold;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Authentication authentication) {
        orderValidator.validateOrderCreation(request.getItems());

        String customerId = jwtService.extractAuth0Id(authentication);

        AddressDto addressDto = userServiceClient.getDefaultAddressByAuth0Id(customerId);
        if (addressDto == null) {
            throw new AddressNotFoundException();
        }

        List<DishDto> dishes = fetchAndValidateDishes(request.getItems());
        Order order = buildOrder(request, customerId, dishes, addressDto);
        orderValidator.validateOrderAmount(order.getTotalAmount());

        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Authentication authentication) {
        String customerId = jwtService.extractAuth0Id(authentication);
        Order order = getOrderOrThrow(orderId);

        validateOrderAccess(order, customerId);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getCustomerOrders(Pageable pageable, Authentication authentication) {
        String customerId = jwtService.extractAuth0Id(authentication);
        Page<Order> ordersPage = orderRepository.findByCustomerId(customerId, pageable);
        return ordersPage.map(orderMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getCookOrders(Pageable pageable, Authentication authentication) {
        String cookId = jwtService.extractAuth0Id(authentication);
        Page<Order> ordersPage = orderRepository.findByCookId(cookId, pageable);
        return ordersPage.map(orderMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getCookOrdersByStatus(String status, Pageable pageable, Authentication authentication) {
        String cookId = jwtService.extractAuth0Id(authentication);

        Order.OrderStatus orderStatus;
        try {
            orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowy status zamówienia: " + status);
        }

        Page<Order> ordersPage = orderRepository.findByCookIdAndStatus(
                cookId, orderStatus, pageable);

        return ordersPage.map(orderMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, Authentication authentication) {
        String cookId = jwtService.extractAuth0Id(authentication);
        Order order = getOrderOrThrow(orderId);

        if (!order.getCookId().equals(cookId)) {
            throw new UnauthorizedOrderAccessException(
                    "Tylko kucharz, do którego należy to zamówienie, może aktualizować jego status");
        }

        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowy status zamówienia: " + request.getStatus());
        }

        orderValidator.validateStatusTransition(order.getStatus(), newStatus);
        order.updateStatus(newStatus);

        if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setActualDeliveryTime(LocalDateTime.now());
        }
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request, Authentication authentication) {
        String userId = jwtService.extractAuth0Id(authentication);
        Order order = getOrderOrThrow(orderId);

        validateOrderAccess(order, userId);
        orderValidator.validateOrderCancellation(order);

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancellationReason(request.getReason());
        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse rateOrder(Long orderId, RateOrderRequest request, Authentication authentication) {
        String customerId = jwtService.extractAuth0Id(authentication);
        Order order = getOrderOrThrow(orderId);

        if (!order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedOrderAccessException(
                    "Tylko klient może ocenić swoje zamówienie");
        }
        orderValidator.validateOrderRating(order);

        order.setDishRating(request.getRating());
        order.setRatedDishId(request.getDishId());
        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    public Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    public void validateOrderAccess(Order order, String userId) {
        if (!order.getCustomerId().equals(userId) && !order.getCookId().equals(userId)) {
            throw new UnauthorizedOrderAccessException();
        }
    }

    private List<DishDto> fetchAndValidateDishes(List<OrderItemRequest> items) {
        List<Long> dishIds = items.stream()
                .map(OrderItemRequest::getDishId)
                .toList();

        List<DishDto> fetchedDishes = dishServiceClient.getDishesByIds(dishIds);

        if (fetchedDishes.size() != new HashSet<>(dishIds).size()) {
            throw new DishNotAvailableException("Nie znaleziono jednego lub więcej dań");
        }

        Map<Long, DishDto> dishMap = fetchedDishes.stream()
                .collect(Collectors.toMap(DishDto::getId, dish -> dish));

        Map<Long, Integer> quantitiesPerDish = items.stream()
                .collect(Collectors.groupingBy(
                        OrderItemRequest::getDishId,
                        Collectors.summingInt(OrderItemRequest::getQuantity)
                ));

        for (Map.Entry<Long, Integer> entry : quantitiesPerDish.entrySet()) {
            Long dishId = entry.getKey();
            Integer totalRequested = entry.getValue();

            DishDto dish = dishMap.get(dishId);

            orderValidator.validateDishAvailability(dish, totalRequested);
        }

        return fetchedDishes;
    }

    private Order buildOrder(CreateOrderRequest request, String customerId,
                             List<DishDto> dishes, AddressDto addressDto) {
        Map<Long, DishDto> dishMap = dishes.stream()
                .collect(Collectors.toMap(DishDto::getId, dish -> dish));

        Order order = Order.builder()
                .customerId(customerId)
                .cookId(request.getCookId())
                .status(Order.OrderStatus.PENDING)
                .customerNotes(request.getCustomerNotes())
                .deliveryAddress(buildDeliveryAddress(addressDto, request))
                .items(new ArrayList<>())
                .build();

        for (OrderItemRequest itemRequest : request.getItems()) {
            DishDto dish = dishMap.get(itemRequest.getDishId());

            String imageUrl = (dish.getImages() != null && !dish.getImages().isEmpty())
                    ? dish.getImages().getFirst()
                    : null;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .dishId(dish.getId())
                    .dishName(dish.getName())
                    .price(dish.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .dishImageUrl(imageUrl)
                    .build();

            orderItem.calculateItemTotal();
            order.addItem(orderItem);
        }
        BigDecimal tempSubtotal = order.getItems().stream()
                .map(OrderItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(tempSubtotal);
        if (tempSubtotal.compareTo(freeDeliveryThreshold) < 0) {
            order.setDeliveryFee(deliveryFee);
        } else {
            order.setDeliveryFee(BigDecimal.ZERO);
        }
        order.calculateTotals();

        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(60));

        return order;
    }

    private DeliveryAddress buildDeliveryAddress(AddressDto addressDto, CreateOrderRequest request) {
        return DeliveryAddress.builder()
                .street(addressDto.getStreet())
                .city(addressDto.getCity())
                .postalCode(addressDto.getPostalCode())
                .apartmentNumber(addressDto.getApartmentNumber())
                .phoneNumber(addressDto.getPhoneNumber())
                .latitude(addressDto.getLatitude())
                .longitude(addressDto.getLongitude())
                .deliveryInstructions(request.getDeliveryInstructions())
                .build();
    }
}
