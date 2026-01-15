package com.flavory.paymentservice.serviceTests;

import com.flavory.paymentservice.dto.request.CreatePaymentIntentRequest;
import com.flavory.paymentservice.dto.request.RefundRequest;
import com.flavory.paymentservice.dto.response.PaymentIntentResponse;
import com.flavory.paymentservice.dto.response.PaymentResponse;
import com.flavory.paymentservice.dto.response.RefundResponse;
import com.flavory.paymentservice.entity.Payment;
import com.flavory.paymentservice.entity.PaymentMethod;
import com.flavory.paymentservice.entity.PaymentStatus;
import com.flavory.paymentservice.exception.*;
import com.flavory.paymentservice.mapper.PaymentMapper;
import com.flavory.paymentservice.messaging.publisher.PaymentEventPublisher;
import com.flavory.paymentservice.repository.PaymentRepository;
import com.flavory.paymentservice.service.StripeService;
import com.flavory.paymentservice.service.impl.PaymentServiceImpl;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Tests")
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private StripeService stripeService;
    @Mock private PaymentMapper paymentMapper;
    @Mock private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static final Long PAYMENT_ID = 1L;
    private static final Long ORDER_ID = 1L;
    private static final String CUSTOMER_ID = "customer123";
    private static final String COOK_ID = "cook123";
    private static final String PAYMENT_INTENT_ID = "pi_123456";
    private static final String CLIENT_SECRET = "secret_123";
    private static final String CHARGE_ID = "ch_123";

    private CreatePaymentIntentRequest createPaymentRequest(BigDecimal amount) {
        return CreatePaymentIntentRequest.builder()
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .cookId(COOK_ID)
                .amount(amount)
                .paymentMethod(PaymentMethod.CARD)
                .metadata("test metadata")
                .build();
    }

    private CreatePaymentIntentRequest createPaymentRequest() {
        return createPaymentRequest(new BigDecimal("100.00"));
    }

    private Payment createPayment(PaymentStatus status) {
        return Payment.builder()
                .id(PAYMENT_ID)
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .cookId(COOK_ID)
                .amount(new BigDecimal("100.00"))
                .currency("PLN")
                .status(status)
                .paymentMethod(PaymentMethod.CARD)
                .stripePaymentIntentId(PAYMENT_INTENT_ID)
                .stripeClientSecret(CLIENT_SECRET)
                .platformFee(new BigDecimal("10.00"))
                .cookPayout(new BigDecimal("90.00"))
                .build();
    }

    private PaymentIntent createMockPaymentIntent(String status) {
        PaymentIntent intent = mock(PaymentIntent.class);
        lenient().when(intent.getId()).thenReturn(PAYMENT_INTENT_ID);
        lenient().when(intent.getClientSecret()).thenReturn(CLIENT_SECRET);
        lenient().when(intent.getStatus()).thenReturn(status);
        lenient().when(intent.getLatestCharge()).thenReturn(CHARGE_ID);
        return intent;
    }

    private Refund createMockRefund() {
        Refund refund = mock(Refund.class);
        when(refund.getId()).thenReturn("re_123");
        return refund;
    }

    private RefundRequest createRefundRequest(BigDecimal amount) {
        return RefundRequest.builder()
                .amount(amount)
                .reason("Customer requested refund")
                .build();
    }

    private PaymentIntentResponse createPaymentIntentResponse() {
        return PaymentIntentResponse.builder()
                .paymentId(PAYMENT_ID)
                .orderId(ORDER_ID)
                .stripePaymentIntentId(PAYMENT_INTENT_ID)
                .clientSecret(CLIENT_SECRET)
                .amount(new BigDecimal("100.00"))
                .currency("PLN")
                .status(PaymentStatus.PENDING)
                .build();
    }

    private PaymentResponse createPaymentResponse(PaymentStatus status) {
        return PaymentResponse.builder()
                .id(PAYMENT_ID)
                .orderId(ORDER_ID)
                .status(status)
                .amount(new BigDecimal("100.00"))
                .build();
    }

    @Nested
    @DisplayName("createPaymentIntent")
    class CreatePaymentIntentTests {

        @Test
        @DisplayName("Should create payment intent successfully")
        void shouldCreatePaymentIntent() {
            CreatePaymentIntentRequest request = createPaymentRequest();
            Payment payment = createPayment(PaymentStatus.PENDING);
            PaymentIntent intent = createMockPaymentIntent("pending");

            when(paymentRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
            when(stripeService.calculatePlatformFee(request.getAmount())).thenReturn(new BigDecimal("10.00"));
            when(stripeService.calculateCookPayout(any(), any())).thenReturn(new BigDecimal("90.00"));
            when(stripeService.createPaymentIntent(any(), any(), any(), any(), any())).thenReturn(intent);
            when(paymentRepository.save(any())).thenReturn(payment);
            when(paymentMapper.toPaymentIntentResponse(payment)).thenReturn(createPaymentIntentResponse());

            PaymentIntentResponse result = paymentService.createPaymentIntent(request);

            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
            verify(paymentRepository).save(any(Payment.class));
            verify(eventPublisher).publishPaymentCreated(any());
        }

        @Test
        @DisplayName("Should throw exception for amount below minimum")
        void shouldThrowForAmountBelowMinimum() {
            CreatePaymentIntentRequest request = createPaymentRequest(new BigDecimal("0.50"));

            assertThatThrownBy(() -> paymentService.createPaymentIntent(request))
                    .isInstanceOf(InvalidPaymentAmountException.class);

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for amount above maximum")
        void shouldThrowForAmountAboveMaximum() {
            CreatePaymentIntentRequest request = createPaymentRequest(new BigDecimal("20000.00"));

            assertThatThrownBy(() -> paymentService.createPaymentIntent(request))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessageContaining("nie może przekraczać");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for null amount")
        void shouldThrowForNullAmount() {
            CreatePaymentIntentRequest request = createPaymentRequest(null);

            assertThatThrownBy(() -> paymentService.createPaymentIntent(request))
                    .isInstanceOf(InvalidPaymentAmountException.class);
        }

        @Test
        @DisplayName("Should throw exception for duplicate payment")
        void shouldThrowForDuplicatePayment() {
            CreatePaymentIntentRequest request = createPaymentRequest();

            when(paymentRepository.existsByOrderId(ORDER_ID)).thenReturn(true);

            assertThatThrownBy(() -> paymentService.createPaymentIntent(request))
                    .isInstanceOf(DuplicatePaymentException.class);

            verify(stripeService, never()).createPaymentIntent(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw PaymentProcessingException on Stripe error")
        void shouldThrowOnStripeError() {
            CreatePaymentIntentRequest request = createPaymentRequest();

            when(paymentRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
            when(stripeService.calculatePlatformFee(any())).thenReturn(new BigDecimal("10.00"));
            when(stripeService.calculateCookPayout(any(), any())).thenReturn(new BigDecimal("90.00"));
            when(stripeService.createPaymentIntent(any(), any(), any(), any(), any()))
                    .thenThrow(new StripeIntegrationException("Stripe error"));

            assertThatThrownBy(() -> paymentService.createPaymentIntent(request))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessageContaining("Nie udało się przetworzyć płatności");
        }
    }

    @Nested
    @DisplayName("confirmPayment")
    class ConfirmPaymentTests {

        @Test
        @DisplayName("Should confirm payment with succeeded status")
        void shouldConfirmPaymentSucceeded() {
            Payment payment = createPayment(PaymentStatus.PENDING);
            PaymentIntent intent = createMockPaymentIntent("succeeded");

            Charge mockCharge = mock(Charge.class);
            Charge.PaymentMethodDetails details = mock(Charge.PaymentMethodDetails.class);
            Charge.PaymentMethodDetails.Card card = mock(Charge.PaymentMethodDetails.Card.class);

            when(card.getLast4()).thenReturn("4242");
            when(card.getBrand()).thenReturn("visa");
            when(details.getCard()).thenReturn(card);
            when(mockCharge.getPaymentMethodDetails()).thenReturn(details);

            when(paymentRepository.findByStripePaymentIntentId(PAYMENT_INTENT_ID))
                    .thenReturn(Optional.of(payment));
            when(stripeService.retrievePaymentIntent(PAYMENT_INTENT_ID)).thenReturn(intent);
            when(paymentRepository.save(payment)).thenReturn(payment);
            when(paymentMapper.toPaymentResponse(payment)).thenReturn(createPaymentResponse(PaymentStatus.SUCCEEDED));

            try (MockedStatic<Charge> mockedChargeStatic = mockStatic(Charge.class)) {
                mockedChargeStatic.when(() -> Charge.retrieve(CHARGE_ID))
                        .thenReturn(mockCharge);

                PaymentResponse result = paymentService.confirmPayment(PAYMENT_INTENT_ID);

                assertThat(result).isNotNull();
                assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
                verify(eventPublisher).publishPaymentSucceeded(payment);
            }
        }

        @Test
        @DisplayName("Should update status to REQUIRES_ACTION")
        void shouldUpdateToRequiresAction() {
            Payment payment = createPayment(PaymentStatus.PENDING);
            PaymentIntent intent = createMockPaymentIntent("requires_action");

            when(paymentRepository.findByStripePaymentIntentId(PAYMENT_INTENT_ID))
                    .thenReturn(Optional.of(payment));
            when(stripeService.retrievePaymentIntent(PAYMENT_INTENT_ID)).thenReturn(intent);
            when(paymentRepository.save(payment)).thenReturn(payment);
            when(paymentMapper.toPaymentResponse(payment)).thenReturn(createPaymentResponse(PaymentStatus.REQUIRES_ACTION));

            PaymentResponse result = paymentService.confirmPayment(PAYMENT_INTENT_ID);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REQUIRES_ACTION);
            verify(eventPublisher, never()).publishPaymentSucceeded(any());
        }

        @Test
        @DisplayName("Should update status to PROCESSING")
        void shouldUpdateToProcessing() {
            Payment payment = createPayment(PaymentStatus.PENDING);
            PaymentIntent intent = createMockPaymentIntent("processing");

            when(paymentRepository.findByStripePaymentIntentId(PAYMENT_INTENT_ID))
                    .thenReturn(Optional.of(payment));
            when(stripeService.retrievePaymentIntent(PAYMENT_INTENT_ID)).thenReturn(intent);
            when(paymentRepository.save(payment)).thenReturn(payment);
            when(paymentMapper.toPaymentResponse(payment)).thenReturn(createPaymentResponse(PaymentStatus.PROCESSING));

            PaymentResponse result = paymentService.confirmPayment(PAYMENT_INTENT_ID);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when not found")
        void shouldThrowWhenPaymentNotFound() {
            when(paymentRepository.findByStripePaymentIntentId(PAYMENT_INTENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.confirmPayment(PAYMENT_INTENT_ID))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cancelPayment")
    class CancelPaymentTests {

        @Test
        @DisplayName("Should cancel pending payment")
        void shouldCancelPendingPayment() {
            Payment payment = createPayment(PaymentStatus.PENDING);
            PaymentIntent intent = createMockPaymentIntent("canceled");

            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
            when(stripeService.cancelPaymentIntent(PAYMENT_INTENT_ID)).thenReturn(intent);
            when(paymentRepository.save(payment)).thenReturn(payment);
            when(paymentMapper.toPaymentResponse(payment)).thenReturn(createPaymentResponse(PaymentStatus.CANCELLED));

            PaymentResponse result = paymentService.cancelPayment(PAYMENT_ID);

            assertThat(result).isNotNull();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            verify(eventPublisher).publishPaymentCancelled(payment);
        }

        @Test
        @DisplayName("Should cancel payment with REQUIRES_ACTION status")
        void shouldCancelRequiresActionPayment() {
            Payment payment = createPayment(PaymentStatus.REQUIRES_ACTION);
            PaymentIntent intent = createMockPaymentIntent("canceled");

            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
            when(stripeService.cancelPaymentIntent(PAYMENT_INTENT_ID)).thenReturn(intent);
            when(paymentRepository.save(payment)).thenReturn(payment);
            when(paymentMapper.toPaymentResponse(payment)).thenReturn(createPaymentResponse(PaymentStatus.CANCELLED));

            paymentService.cancelPayment(PAYMENT_ID);

            verify(stripeService).cancelPaymentIntent(PAYMENT_INTENT_ID);
        }

        @Test
        @DisplayName("Should throw exception when payment cannot be cancelled")
        void shouldThrowWhenCannotBeCancelled() {
            Payment payment = createPayment(PaymentStatus.SUCCEEDED);

            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.cancelPayment(PAYMENT_ID))
                    .isInstanceOf(InvalidPaymentStateException.class);

            verify(stripeService, never()).cancelPaymentIntent(any());
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.cancelPayment(PAYMENT_ID))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getPaymentById")
    class GetPaymentByIdTests {

        @Test
        @DisplayName("Should return payment by ID")
        void shouldReturnPaymentById() {
            Payment payment = createPayment(PaymentStatus.SUCCEEDED);

            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
            when(paymentMapper.toPaymentResponse(payment)).thenReturn(createPaymentResponse(PaymentStatus.SUCCEEDED));

            PaymentResponse result = paymentService.getPaymentById(PAYMENT_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PAYMENT_ID);
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPaymentById(PAYMENT_ID))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getPaymentByOrderId")
    class GetPaymentByOrderIdTests {

        @Test
        @DisplayName("Should return payment by order ID")
        void shouldReturnPaymentByOrderId() {
            Payment payment = createPayment(PaymentStatus.SUCCEEDED);

            when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
            when(paymentMapper.toPaymentResponse(payment)).thenReturn(createPaymentResponse(PaymentStatus.SUCCEEDED));

            PaymentResponse result = paymentService.getPaymentByOrderId(ORDER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPaymentByOrderId(ORDER_ID))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCustomerPayments")
    class GetCustomerPaymentsTests {

        @Test
        @DisplayName("Should return customer payments")
        void shouldReturnCustomerPayments() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Payment> payments = List.of(createPayment(PaymentStatus.SUCCEEDED));
            Page<Payment> page = new PageImpl<>(payments);

            when(paymentRepository.findByCustomerId(CUSTOMER_ID, pageable)).thenReturn(page);
            when(paymentMapper.toPaymentResponse(any())).thenReturn(createPaymentResponse(PaymentStatus.SUCCEEDED));

            Page<PaymentResponse> result = paymentService.getCustomerPayments(CUSTOMER_ID, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty page when no payments")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Payment> emptyPage = new PageImpl<>(List.of());

            when(paymentRepository.findByCustomerId(CUSTOMER_ID, pageable)).thenReturn(emptyPage);

            Page<PaymentResponse> result = paymentService.getCustomerPayments(CUSTOMER_ID, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCookPayments")
    class GetCookPaymentsTests {

        @Test
        @DisplayName("Should return cook payments")
        void shouldReturnCookPayments() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Payment> payments = List.of(createPayment(PaymentStatus.SUCCEEDED));
            Page<Payment> page = new PageImpl<>(payments);

            when(paymentRepository.findByCookId(COOK_ID, pageable)).thenReturn(page);
            when(paymentMapper.toPaymentResponse(any())).thenReturn(createPaymentResponse(PaymentStatus.SUCCEEDED));

            Page<PaymentResponse> result = paymentService.getCookPayments(COOK_ID, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("refundPayment")
    class RefundPaymentTests {

        @Test
        @DisplayName("Should refund payment successfully")
        void shouldRefundPayment() {
            Payment payment = createPayment(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(LocalDateTime.now().minusDays(1));

            RefundRequest request = createRefundRequest(new BigDecimal("50.00"));
            Refund refund = createMockRefund();

            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
            when(stripeService.createRefund(PAYMENT_INTENT_ID, request)).thenReturn(refund);
            when(paymentRepository.save(payment)).thenReturn(payment);

            RefundResponse result = paymentService.refundPayment(PAYMENT_ID, request);

            assertThat(result).isNotNull();
            assertThat(result.getRefundAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            verify(eventPublisher).publishPaymentRefunded(payment);
        }

        @Test
        @DisplayName("Should throw exception when payment cannot be refunded")
        void shouldThrowWhenCannotBeRefunded() {
            Payment payment = createPayment(PaymentStatus.PENDING);
            RefundRequest request = createRefundRequest(new BigDecimal("50.00"));

            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.refundPayment(PAYMENT_ID, request))
                    .isInstanceOf(RefundNotAllowedException.class);
        }

        @Test
        @DisplayName("Should throw exception when refund amount exceeds payment amount")
        void shouldThrowWhenRefundExceedsPayment() {
            Payment payment = createPayment(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(LocalDateTime.now().minusDays(1));

            RefundRequest request = createRefundRequest(new BigDecimal("150.00"));

            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.refundPayment(PAYMENT_ID, request))
                    .isInstanceOf(RefundNotAllowedException.class)
                    .hasMessageContaining("nie może przekraczać");
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            RefundRequest request = createRefundRequest(new BigDecimal("50.00"));

            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.refundPayment(PAYMENT_ID, request))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("markPaymentAsFailed")
    class MarkPaymentAsFailedTests {

        @Test
        @DisplayName("Should mark payment as failed")
        void shouldMarkPaymentAsFailed() {
            Payment payment = createPayment(PaymentStatus.PENDING);

            when(paymentRepository.findByStripePaymentIntentId(PAYMENT_INTENT_ID))
                    .thenReturn(Optional.of(payment));
            when(paymentRepository.save(payment)).thenReturn(payment);
            when(paymentMapper.toPaymentResponse(payment)).thenReturn(createPaymentResponse(PaymentStatus.FAILED));

            PaymentResponse result = paymentService.markPaymentAsFailed(
                    PAYMENT_INTENT_ID, "card_declined", "Card was declined"
            );

            assertThat(result).isNotNull();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureCode()).isEqualTo("card_declined");
            assertThat(payment.getFailureMessage()).isEqualTo("Card was declined");
            verify(eventPublisher).publishPaymentFailed(payment);
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(paymentRepository.findByStripePaymentIntentId(PAYMENT_INTENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.markPaymentAsFailed(
                    PAYMENT_INTENT_ID, "error", "Error message"
            )).isInstanceOf(PaymentNotFoundException.class);
        }
    }
}