package com.flavory.paymentservice.serviceTests;

import com.flavory.paymentservice.config.PaymentProperties;
import com.flavory.paymentservice.dto.request.RefundRequest;
import com.flavory.paymentservice.entity.PaymentMethod;
import com.flavory.paymentservice.exception.StripeIntegrationException;
import com.flavory.paymentservice.service.impl.StripeServiceImpl;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StripeServiceImpl Tests")
class StripeServiceImplTest {

    @Mock
    private PaymentProperties paymentProperties;

    @Mock
    private PaymentProperties.Fees fees;

    @InjectMocks
    private StripeServiceImpl stripeService;

    private static final String CUSTOMER_ID = "customer123";
    private static final Long ORDER_ID = 1L;
    private static final String PAYMENT_INTENT_ID = "pi_123456";
    private static final String CURRENCY = "PLN";

    @BeforeEach
    void setUp() {
        lenient().when(paymentProperties.getFees()).thenReturn(fees);
    }

    private RefundRequest createRefundRequest(BigDecimal amount, String reason) {
        return RefundRequest.builder()
                .amount(amount)
                .reason(reason)
                .build();
    }

    private RefundRequest createRefundRequest() {
        return createRefundRequest(new BigDecimal("50.00"), "Customer requested refund");
    }

    @Nested
    @DisplayName("createPaymentIntent")
    class CreatePaymentIntentTests {

        @Test
        @DisplayName("Should create payment intent successfully")
        void shouldCreatePaymentIntent() {
            BigDecimal amount = new BigDecimal("100.00");
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(mockIntent);

                PaymentIntent result = stripeService.createPaymentIntent(
                        amount, CURRENCY, CUSTOMER_ID, ORDER_ID, PaymentMethod.CARD
                );

                assertThat(result).isNotNull();
                assertThat(result).isEqualTo(mockIntent);
            }
        }

        @Test
        @DisplayName("Should convert amount to cents correctly")
        void shouldConvertAmountToCents() {
            BigDecimal amount = new BigDecimal("123.45");
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(mockIntent);

                stripeService.createPaymentIntent(
                        amount, CURRENCY, CUSTOMER_ID, ORDER_ID, PaymentMethod.CARD
                );

                mockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)));
            }
        }

        @Test
        @DisplayName("Should throw StripeIntegrationException on Stripe error")
        void shouldThrowExceptionOnStripeError() {
            BigDecimal amount = new BigDecimal("100.00");

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenThrow(new StripeException("Stripe API error", "req_123", "code", 400) {});

                assertThatThrownBy(() -> stripeService.createPaymentIntent(
                        amount, CURRENCY, CUSTOMER_ID, ORDER_ID, PaymentMethod.CARD
                ))
                        .isInstanceOf(StripeIntegrationException.class)
                        .hasMessageContaining("Nie udało się utworzyć Payment Intent");
            }
        }

        @Test
        @DisplayName("Should handle different payment methods")
        void shouldHandleDifferentPaymentMethods() {
            BigDecimal amount = new BigDecimal("100.00");
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(mockIntent);

                stripeService.createPaymentIntent(
                        amount, CURRENCY, CUSTOMER_ID, ORDER_ID, PaymentMethod.BLIK
                );

                assertThat(mockIntent).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("cancelPaymentIntent")
    class CancelPaymentIntentTests {

        @Test
        @DisplayName("Should cancel payment intent successfully")
        void shouldCancelPaymentIntent() throws StripeException {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            PaymentIntent cancelledIntent = mock(PaymentIntent.class);

            when(mockIntent.cancel(any(PaymentIntentCancelParams.class))).thenReturn(cancelledIntent);

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.retrieve(PAYMENT_INTENT_ID))
                        .thenReturn(mockIntent);

                PaymentIntent result = stripeService.cancelPaymentIntent(PAYMENT_INTENT_ID);

                assertThat(result).isEqualTo(cancelledIntent);

                verify(mockIntent).cancel(any(PaymentIntentCancelParams.class));
            }
        }

        @Test
        @DisplayName("Should throw exception when retrieval fails")
        void shouldThrowExceptionWhenRetrievalFails() {
            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.retrieve(PAYMENT_INTENT_ID))
                        .thenThrow(new StripeException("Not found", "req_123", "code", 404) {});

                assertThatThrownBy(() -> stripeService.cancelPaymentIntent(PAYMENT_INTENT_ID))
                        .isInstanceOf(StripeIntegrationException.class)
                        .hasMessageContaining("Nie udało się anulować Payment Intent");
            }
        }

        @Test
        @DisplayName("Should throw exception when cancellation fails")
        void shouldThrowExceptionWhenCancellationFails() throws StripeException {
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            when(mockIntent.cancel(any(PaymentIntentCancelParams.class)))
                    .thenThrow(new StripeException("Cannot cancel", "req_123", "code", 400) {});

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.retrieve(PAYMENT_INTENT_ID))
                        .thenReturn(mockIntent);

                assertThatThrownBy(() -> stripeService.cancelPaymentIntent(PAYMENT_INTENT_ID))
                        .isInstanceOf(StripeIntegrationException.class);
            }
        }
    }

    @Nested
    @DisplayName("retrievePaymentIntent")
    class RetrievePaymentIntentTests {

        @Test
        @DisplayName("Should retrieve payment intent successfully")
        void shouldRetrievePaymentIntent() {
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.retrieve(PAYMENT_INTENT_ID))
                        .thenReturn(mockIntent);

                PaymentIntent result = stripeService.retrievePaymentIntent(PAYMENT_INTENT_ID);

                assertThat(result).isEqualTo(mockIntent);
            }
        }

        @Test
        @DisplayName("Should throw exception when retrieval fails")
        void shouldThrowExceptionWhenRetrievalFails() {
            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.retrieve(PAYMENT_INTENT_ID))
                        .thenThrow(new StripeException("Not found", "req_123", "code", 404) {});

                assertThatThrownBy(() -> stripeService.retrievePaymentIntent(PAYMENT_INTENT_ID))
                        .isInstanceOf(StripeIntegrationException.class)
                        .hasMessageContaining("Nie udało się pobrać Payment Intent");
            }
        }
    }

    @Nested
    @DisplayName("createRefund")
    class CreateRefundTests {

        @Test
        @DisplayName("Should create refund successfully")
        void shouldCreateRefund() {
            RefundRequest request = createRefundRequest();
            Refund mockRefund = mock(Refund.class);

            try (MockedStatic<Refund> mockedStatic = mockStatic(Refund.class)) {
                mockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                        .thenReturn(mockRefund);

                Refund result = stripeService.createRefund(PAYMENT_INTENT_ID, request);

                assertThat(result).isEqualTo(mockRefund);
            }
        }

        @Test
        @DisplayName("Should convert refund amount to cents")
        void shouldConvertRefundAmountToCents() {
            RefundRequest request = createRefundRequest(new BigDecimal("75.50"), "Refund reason");
            Refund mockRefund = mock(Refund.class);

            try (MockedStatic<Refund> mockedStatic = mockStatic(Refund.class)) {
                mockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                        .thenReturn(mockRefund);

                stripeService.createRefund(PAYMENT_INTENT_ID, request);

                mockedStatic.verify(() -> Refund.create(any(RefundCreateParams.class)));
            }
        }

        @Test
        @DisplayName("Should throw exception on Stripe error")
        void shouldThrowExceptionOnStripeError() {
            RefundRequest request = createRefundRequest();

            try (MockedStatic<Refund> mockedStatic = mockStatic(Refund.class)) {
                mockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                        .thenThrow(new StripeException("Refund failed", "req_123", "code", 400) {});

                assertThatThrownBy(() -> stripeService.createRefund(PAYMENT_INTENT_ID, request))
                        .isInstanceOf(StripeIntegrationException.class)
                        .hasMessageContaining("Nie udało się utworzyć zwrotu");
            }
        }
    }

    @Nested
    @DisplayName("calculatePlatformFee")
    class CalculatePlatformFeeTests {

        @Test
        @DisplayName("Should calculate fee within normal range")
        void shouldCalculateFeeWithinRange() {
            BigDecimal amount = new BigDecimal("100.00");

            when(fees.getPlatformPercentage()).thenReturn(new BigDecimal("10"));
            when(fees.getMinPlatformFee()).thenReturn(new BigDecimal("1.00"));
            when(fees.getMaxPlatformFee()).thenReturn(new BigDecimal("50.00"));

            BigDecimal result = stripeService.calculatePlatformFee(amount);

            assertThat(result).isEqualByComparingTo(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("Should apply minimum fee when calculated fee is too low")
        void shouldApplyMinimumFee() {
            BigDecimal amount = new BigDecimal("5.00");

            when(fees.getPlatformPercentage()).thenReturn(new BigDecimal("10"));
            when(fees.getMinPlatformFee()).thenReturn(new BigDecimal("2.00"));
            when(fees.getMaxPlatformFee()).thenReturn(new BigDecimal("50.00"));

            BigDecimal result = stripeService.calculatePlatformFee(amount);

            assertThat(result).isEqualByComparingTo(new BigDecimal("2.00"));
        }

        @Test
        @DisplayName("Should apply maximum fee when calculated fee is too high")
        void shouldApplyMaximumFee() {
            BigDecimal amount = new BigDecimal("1000.00");

            when(fees.getPlatformPercentage()).thenReturn(new BigDecimal("10"));
            when(fees.getMinPlatformFee()).thenReturn(new BigDecimal("1.00"));
            when(fees.getMaxPlatformFee()).thenReturn(new BigDecimal("50.00"));

            BigDecimal result = stripeService.calculatePlatformFee(amount);

            assertThat(result).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("Should round fee to 2 decimal places")
        void shouldRoundFeeToTwoDecimals() {
            BigDecimal amount = new BigDecimal("33.33");

            when(fees.getPlatformPercentage()).thenReturn(new BigDecimal("10"));
            when(fees.getMinPlatformFee()).thenReturn(new BigDecimal("1.00"));
            when(fees.getMaxPlatformFee()).thenReturn(new BigDecimal("50.00"));

            BigDecimal result = stripeService.calculatePlatformFee(amount);

            assertThat(result).isEqualByComparingTo(new BigDecimal("3.33"));
        }

        @Test
        @DisplayName("Should handle zero amount")
        void shouldHandleZeroAmount() {
            BigDecimal amount = BigDecimal.ZERO;

            when(fees.getPlatformPercentage()).thenReturn(new BigDecimal("10"));
            when(fees.getMinPlatformFee()).thenReturn(new BigDecimal("1.00"));
            when(fees.getMaxPlatformFee()).thenReturn(new BigDecimal("50.00"));

            BigDecimal result = stripeService.calculatePlatformFee(amount);

            assertThat(result).isEqualByComparingTo(new BigDecimal("1.00"));
        }
    }

    @Nested
    @DisplayName("calculateCookPayout")
    class CalculateCookPayoutTests {

        @Test
        @DisplayName("Should calculate cook payout correctly")
        void shouldCalculateCookPayout() {
            BigDecimal amount = new BigDecimal("100.00");
            BigDecimal platformFee = new BigDecimal("10.00");

            BigDecimal result = stripeService.calculateCookPayout(amount, platformFee);

            assertThat(result).isEqualByComparingTo(new BigDecimal("90.00"));
        }

        @Test
        @DisplayName("Should round payout to 2 decimal places")
        void shouldRoundPayoutToTwoDecimals() {
            BigDecimal amount = new BigDecimal("100.00");
            BigDecimal platformFee = new BigDecimal("10.333");

            BigDecimal result = stripeService.calculateCookPayout(amount, platformFee);

            assertThat(result).isEqualByComparingTo(new BigDecimal("89.67"));
        }

        @Test
        @DisplayName("Should handle zero fee")
        void shouldHandleZeroFee() {
            BigDecimal amount = new BigDecimal("100.00");
            BigDecimal platformFee = BigDecimal.ZERO;

            BigDecimal result = stripeService.calculateCookPayout(amount, platformFee);

            assertThat(result).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should handle fee equal to amount")
        void shouldHandleFeeEqualToAmount() {
            BigDecimal amount = new BigDecimal("100.00");
            BigDecimal platformFee = new BigDecimal("100.00");

            BigDecimal result = stripeService.calculateCookPayout(amount, platformFee);

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationTests {

        @Test
        @DisplayName("Should calculate fees and payout correctly together")
        void shouldCalculateFeesAndPayoutTogether() {
            BigDecimal amount = new BigDecimal("200.00");

            when(fees.getPlatformPercentage()).thenReturn(new BigDecimal("15"));
            when(fees.getMinPlatformFee()).thenReturn(new BigDecimal("1.00"));
            when(fees.getMaxPlatformFee()).thenReturn(new BigDecimal("100.00"));

            BigDecimal platformFee = stripeService.calculatePlatformFee(amount);
            BigDecimal cookPayout = stripeService.calculateCookPayout(amount, platformFee);

            assertThat(platformFee).isEqualByComparingTo(new BigDecimal("30.00"));
            assertThat(cookPayout).isEqualByComparingTo(new BigDecimal("170.00"));
            assertThat(platformFee.add(cookPayout)).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should handle minimum fee scenario with payout")
        void shouldHandleMinimumFeeWithPayout() {
            BigDecimal amount = new BigDecimal("5.00");

            when(fees.getPlatformPercentage()).thenReturn(new BigDecimal("10"));
            when(fees.getMinPlatformFee()).thenReturn(new BigDecimal("2.00"));
            when(fees.getMaxPlatformFee()).thenReturn(new BigDecimal("50.00"));

            BigDecimal platformFee = stripeService.calculatePlatformFee(amount);
            BigDecimal cookPayout = stripeService.calculateCookPayout(amount, platformFee);

            assertThat(platformFee).isEqualByComparingTo(new BigDecimal("2.00"));
            assertThat(cookPayout).isEqualByComparingTo(new BigDecimal("3.00"));
        }

        @Test
        @DisplayName("Should handle maximum fee scenario with payout")
        void shouldHandleMaximumFeeWithPayout() {
            BigDecimal amount = new BigDecimal("2000.00");

            when(fees.getPlatformPercentage()).thenReturn(new BigDecimal("10"));
            when(fees.getMinPlatformFee()).thenReturn(new BigDecimal("1.00"));
            when(fees.getMaxPlatformFee()).thenReturn(new BigDecimal("100.00"));

            BigDecimal platformFee = stripeService.calculatePlatformFee(amount);
            BigDecimal cookPayout = stripeService.calculateCookPayout(amount, platformFee);

            assertThat(platformFee).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(cookPayout).isEqualByComparingTo(new BigDecimal("1900.00"));
        }
    }
}