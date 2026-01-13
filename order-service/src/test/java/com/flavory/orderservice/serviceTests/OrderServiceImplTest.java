package com.flavory.orderservice.serviceTests;

import com.flavory.orderservice.client.DishServiceClient;
import com.flavory.orderservice.client.UserServiceClient;
import com.flavory.orderservice.dto.request.*;
import com.flavory.orderservice.dto.response.*;
import com.flavory.orderservice.entity.DeliveryAddress;
import com.flavory.orderservice.entity.Order;
import com.flavory.orderservice.exception.*;
import com.flavory.orderservice.mapper.OrderMapper;
import com.flavory.orderservice.messaging.publisher.OrderEventPublisher;
import com.flavory.orderservice.repository.OrderRepository;
import com.flavory.orderservice.security.JwtService;
import com.flavory.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Tests")
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private JwtService jwtService;
    @Mock private OrderMapper orderMapper;
    @Mock private UserServiceClient userServiceClient;
    @Mock private DishServiceClient dishServiceClient;
    @Mock private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final String CUSTOMER_ID = "customer123";
    private static final String COOK_ID = "cook123";
    private static final Long ORDER_ID = 1L;
    private static final Long DISH_ID = 1L;

    private Authentication createAuth() {
        return mock(Authentication.class);
    }

    private Order createOrder(Order.OrderStatus status) {
        return Order.builder()
                .id(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .customerName("John Doe")
                .cookId(COOK_ID)
                .cookName("Chef Cook")
                .status(status)
                .subtotal(new BigDecimal("50.00"))
                .deliveryFee(new BigDecimal("5.00"))
                .totalAmount(new BigDecimal("55.00"))
                .deliveryAddress(createDeliveryAddress())
                .pickupAddress(createDeliveryAddress())
                .items(new ArrayList<>())
                .build();
    }

    private CreateOrderRequest createOrderRequest() {
        return CreateOrderRequest.builder()
                .cookId(COOK_ID)
                .cookIdlong(1L)
                .items(List.of(OrderItemRequest.builder()
                        .dishId(DISH_ID)
                        .quantity(2)
                        .build()))
                .customerNotes("Please deliver fast")
                .deliveryInstructions("Ring the bell")
                .paymentMethod("CARD")
                .build();
    }

    private DishDto createDishDto() {
        return DishDto.builder()
                .id(DISH_ID)
                .cookId(COOK_ID)
                .name("Test Dish")
                .price(new BigDecimal("25.00"))
                .available(true)
                .currentStock(10)
                .images(List.of("image1.jpg"))
                .build();
    }

    private AddressDto createAddressDto() {
        return AddressDto.builder()
                .street("Main Street")
                .city("Warsaw")
                .postalCode("00-001")
                .apartmentNumber("10")
                .phoneNumber("+48123456789")
                .latitude(52.2297)
                .longitude(21.0122)
                .build();
    }

    private UserDto createUserDto() {
        return UserDto.builder()
                .id(1L)
                .firstName("Chef")
                .lastName("Cook")
                .fullName("Chef Cook")
                .build();
    }

    private OrderResponse createOrderResponse() {
        return OrderResponse.builder()
                .id(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .cookId(COOK_ID)
                .status("PENDING")
                .totalAmount(new BigDecimal("55.00"))
                .build();
    }

    private DeliveryAddress createDeliveryAddress() {
        return DeliveryAddress.builder()
                .street("Test Street")
                .city("Test City")
                .postalCode("00-000")
                .apartmentNumber("1")
                .phoneNumber("123456789")
                .latitude(52.0)
                .longitude(21.0)
                .build();
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully")
        void shouldCreateOrder() {
            ReflectionTestUtils.setField(orderService, "deliveryFee", new BigDecimal("5.00"));
            ReflectionTestUtils.setField(orderService, "freeDeliveryThreshold", new BigDecimal("100.00"));

            CreateOrderRequest request = createOrderRequest();
            Authentication auth = createAuth();
            Order order = createOrder(Order.OrderStatus.PENDING);

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(jwtService.extractUserName(auth)).thenReturn("John Doe");
            when(userServiceClient.getUserProfile(1L)).thenReturn(createUserDto());
            when(userServiceClient.getDefaultAddressByAuth0Id(CUSTOMER_ID)).thenReturn(createAddressDto());
            when(userServiceClient.getDefaultAddressByAuth0Id(COOK_ID)).thenReturn(createAddressDto());
            when(dishServiceClient.getDishesByIds(any())).thenReturn(List.of(createDishDto()));
            when(orderRepository.save(any())).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(createOrderResponse());

            OrderResponse result = orderService.createOrder(request, auth);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(ORDER_ID);
            verify(orderRepository).save(any(Order.class));
            verify(orderEventPublisher).publishOrderPlaced(any());
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when customer address not found")
        void shouldThrowWhenCustomerAddressNotFound() {
            CreateOrderRequest request = createOrderRequest();
            Authentication auth = createAuth();

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(jwtService.extractUserName(auth)).thenReturn("John Doe");
            when(userServiceClient.getUserProfile(1L)).thenReturn(createUserDto());
            when(userServiceClient.getDefaultAddressByAuth0Id(CUSTOMER_ID)).thenReturn(null);

            assertThatThrownBy(() -> orderService.createOrder(request, auth))
                    .isInstanceOf(AddressNotFoundException.class);

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when cook address not found")
        void shouldThrowWhenCookAddressNotFound() {
            CreateOrderRequest request = createOrderRequest();
            Authentication auth = createAuth();

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(jwtService.extractUserName(auth)).thenReturn("John Doe");
            when(userServiceClient.getUserProfile(1L)).thenReturn(createUserDto());
            when(userServiceClient.getDefaultAddressByAuth0Id(CUSTOMER_ID)).thenReturn(createAddressDto());
            when(userServiceClient.getDefaultAddressByAuth0Id(COOK_ID)).thenReturn(null);

            assertThatThrownBy(() -> orderService.createOrder(request, auth))
                    .isInstanceOf(AddressNotFoundException.class);

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should apply free delivery when threshold reached")
        void shouldApplyFreeDelivery() {
            ReflectionTestUtils.setField(orderService, "deliveryFee", new BigDecimal("5.00"));
            ReflectionTestUtils.setField(orderService, "freeDeliveryThreshold", new BigDecimal("50.00"));

            CreateOrderRequest request = createOrderRequest();
            Authentication auth = createAuth();
            Order order = createOrder(Order.OrderStatus.PENDING);

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(jwtService.extractUserName(auth)).thenReturn("John Doe");
            when(userServiceClient.getUserProfile(1L)).thenReturn(createUserDto());
            when(userServiceClient.getDefaultAddressByAuth0Id(anyString())).thenReturn(createAddressDto());
            when(dishServiceClient.getDishesByIds(any())).thenReturn(List.of(createDishDto()));
            when(orderRepository.save(any())).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(createOrderResponse());

            orderService.createOrder(request, auth);

            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("getOrderById")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order for customer")
        void shouldReturnOrderForCustomer() {
            Order order = createOrder(Order.OrderStatus.PENDING);
            Authentication auth = createAuth();

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderMapper.toResponse(order)).thenReturn(createOrderResponse());

            OrderResponse result = orderService.getOrderById(ORDER_ID, auth);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("Should throw OrderNotFoundException when order not found")
        void shouldThrowWhenOrderNotFound() {
            Authentication auth = createAuth();

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID, auth))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedOrderAccessException for unauthorized user")
        void shouldThrowWhenUnauthorized() {
            Order order = createOrder(Order.OrderStatus.PENDING);
            Authentication auth = createAuth();

            when(jwtService.extractAuth0Id(auth)).thenReturn("different_user");
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID, auth))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }
    }

    @Nested
    @DisplayName("getCustomerOrders")
    class GetCustomerOrdersTests {

        @Test
        @DisplayName("Should return customer orders")
        void shouldReturnCustomerOrders() {
            Authentication auth = createAuth();
            Pageable pageable = PageRequest.of(0, 10);
            List<Order> orders = List.of(createOrder(Order.OrderStatus.PENDING));
            Page<Order> page = new PageImpl<>(orders);

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(orderRepository.findByCustomerId(CUSTOMER_ID, pageable)).thenReturn(page);
            when(orderMapper.toSummaryResponse(any())).thenReturn(OrderSummaryResponse.builder().build());

            Page<OrderSummaryResponse> result = orderService.getCustomerOrders(pageable, auth);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty page when no orders")
        void shouldReturnEmptyPage() {
            Authentication auth = createAuth();
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> emptyPage = new PageImpl<>(List.of());

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(orderRepository.findByCustomerId(CUSTOMER_ID, pageable)).thenReturn(emptyPage);

            Page<OrderSummaryResponse> result = orderService.getCustomerOrders(pageable, auth);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCookOrders")
    class GetCookOrdersTests {

        @Test
        @DisplayName("Should return cook orders")
        void shouldReturnCookOrders() {
            Authentication auth = createAuth();
            Pageable pageable = PageRequest.of(0, 10);
            List<Order> orders = List.of(createOrder(Order.OrderStatus.CONFIRMED));
            Page<Order> page = new PageImpl<>(orders);

            when(jwtService.extractAuth0Id(auth)).thenReturn(COOK_ID);
            when(orderRepository.findByCookId(COOK_ID, pageable)).thenReturn(page);
            when(orderMapper.toSummaryResponse(any())).thenReturn(OrderSummaryResponse.builder().build());

            Page<OrderSummaryResponse> result = orderService.getCookOrders(pageable, auth);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getCookOrdersByStatus")
    class GetCookOrdersByStatusTests {

        @Test
        @DisplayName("Should return orders by status")
        void shouldReturnOrdersByStatus() {
            Authentication auth = createAuth();
            Pageable pageable = PageRequest.of(0, 10);
            List<Order> orders = List.of(createOrder(Order.OrderStatus.PREPARING));
            Page<Order> page = new PageImpl<>(orders);

            when(jwtService.extractAuth0Id(auth)).thenReturn(COOK_ID);
            when(orderRepository.findByCookIdAndStatus(COOK_ID, Order.OrderStatus.PREPARING, pageable))
                    .thenReturn(page);
            when(orderMapper.toSummaryResponse(any())).thenReturn(OrderSummaryResponse.builder().build());

            Page<OrderSummaryResponse> result = orderService.getCookOrdersByStatus("PREPARING", pageable, auth);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowForInvalidStatus() {
            Authentication auth = createAuth();
            Pageable pageable = PageRequest.of(0, 10);

            when(jwtService.extractAuth0Id(auth)).thenReturn(COOK_ID);

            assertThatThrownBy(() -> orderService.getCookOrdersByStatus("INVALID", pageable, auth))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nieprawidłowy status");
        }
    }

    @Nested
    @DisplayName("updateOrderStatus")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update order status to READY and publish event")
        void shouldUpdateToReadyAndPublishEvent() {
            Order order = createOrder(Order.OrderStatus.PREPARING);
            Authentication auth = createAuth();
            UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                    .status("READY")
                    .build();

            when(jwtService.extractAuth0Id(auth)).thenReturn(COOK_ID);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(createOrderResponse());

            orderService.updateOrderStatus(ORDER_ID, request, auth);

            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.READY);
            verify(orderEventPublisher).publishOrderReady(any());
        }

        @Test
        @DisplayName("Should update to DELIVERED and set delivery time")
        void shouldUpdateToDeliveredAndSetTime() {
            Order order = createOrder(Order.OrderStatus.IN_DELIVERY);
            Authentication auth = createAuth();
            UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                    .status("DELIVERED")
                    .build();

            when(jwtService.extractAuth0Id(auth)).thenReturn(COOK_ID);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(createOrderResponse());

            orderService.updateOrderStatus(ORDER_ID, request, auth);

            assertThat(order.getActualDeliveryTime()).isNotNull();
            verify(orderEventPublisher).publishOrderCompleted(any());
        }

        @Test
        @DisplayName("Should throw exception when not cook's order")
        void shouldThrowWhenNotCooksOrder() {
            Order order = createOrder(Order.OrderStatus.PENDING);
            Authentication auth = createAuth();
            UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                    .status("CONFIRMED")
                    .build();

            when(jwtService.extractAuth0Id(auth)).thenReturn("different_cook");
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.updateOrderStatus(ORDER_ID, request, auth))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowForInvalidStatus() {
            Order order = createOrder(Order.OrderStatus.PENDING);
            Authentication auth = createAuth();
            UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                    .status("INVALID")
                    .build();

            when(jwtService.extractAuth0Id(auth)).thenReturn(COOK_ID);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.updateOrderStatus(ORDER_ID, request, auth))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order successfully")
        void shouldCancelOrder() {
            Order order = createOrder(Order.OrderStatus.PENDING);
            Authentication auth = createAuth();
            CancelOrderRequest request = CancelOrderRequest.builder()
                    .reason("Changed my mind")
                    .build();

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(createOrderResponse());

            OrderResponse result = orderService.cancelOrder(ORDER_ID, request, auth);

            assertThat(result).isNotNull();
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
            assertThat(order.getCancellationReason()).isEqualTo("Changed my mind");
            verify(orderEventPublisher).publishOrderCancelled(any());
        }

        @Test
        @DisplayName("Should throw exception when unauthorized")
        void shouldThrowWhenUnauthorized() {
            Order order = createOrder(Order.OrderStatus.PENDING);
            Authentication auth = createAuth();
            CancelOrderRequest request = CancelOrderRequest.builder()
                    .reason("Test reason")
                    .build();

            when(jwtService.extractAuth0Id(auth)).thenReturn("different_user");
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID, request, auth))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }
    }

    @Nested
    @DisplayName("rateOrder")
    class RateOrderTests {

        @Test
        @DisplayName("Should rate order successfully")
        void shouldRateOrder() {
            Order order = createOrder(Order.OrderStatus.DELIVERED);
            Authentication auth = createAuth();
            RateOrderRequest request = RateOrderRequest.builder()
                    .rating(new BigDecimal("4.5"))
                    .dishId(DISH_ID)
                    .build();

            when(jwtService.extractAuth0Id(auth)).thenReturn(CUSTOMER_ID);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(createOrderResponse());

            OrderResponse result = orderService.rateOrder(ORDER_ID, request, auth);

            assertThat(result).isNotNull();
            assertThat(order.getDishRating()).isEqualByComparingTo(new BigDecimal("4.5"));
            assertThat(order.getRatedDishId()).isEqualTo(DISH_ID);
            verify(orderEventPublisher).publishOrderCompleted(any());
        }

        @Test
        @DisplayName("Should throw exception when not customer's order")
        void shouldThrowWhenNotCustomersOrder() {
            Order order = createOrder(Order.OrderStatus.DELIVERED);
            Authentication auth = createAuth();
            RateOrderRequest request = RateOrderRequest.builder()
                    .rating(new BigDecimal("4.5"))
                    .dishId(DISH_ID)
                    .build();

            when(jwtService.extractAuth0Id(auth)).thenReturn("different_customer");
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.rateOrder(ORDER_ID, request, auth))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }
    }

    @Nested
    @DisplayName("validateOrderAccess")
    class ValidateOrderAccessTests {

        @Test
        @DisplayName("Should allow access for customer")
        void shouldAllowAccessForCustomer() {
            Order order = createOrder(Order.OrderStatus.PENDING);

            orderService.validateOrderAccess(order, CUSTOMER_ID);
        }

        @Test
        @DisplayName("Should allow access for cook")
        void shouldAllowAccessForCook() {
            Order order = createOrder(Order.OrderStatus.PENDING);

            orderService.validateOrderAccess(order, COOK_ID);
        }

        @Test
        @DisplayName("Should deny access for unauthorized user")
        void shouldDenyAccessForUnauthorized() {
            Order order = createOrder(Order.OrderStatus.PENDING);

            assertThatThrownBy(() -> orderService.validateOrderAccess(order, "different_user"))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }
    }
}