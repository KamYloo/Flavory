package com.flavory.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_stripe_payment_intent_id", columnList = "stripe_payment_intent_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "order_id", unique = true)
    private Long orderId;

    @Column(nullable = false, name = "customer_id")
    private String customerId;

    @Column(nullable = false, name = "cook_id")
    private String cookId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "PLN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "payment_method", length = 30)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    @Column(name = "stripe_payment_intent_id", length = 100, unique = true)
    private String stripePaymentIntentId;

    @Column(name = "stripe_charge_id", length = 100)
    private String stripeChargeId;

    @Column(name = "stripe_client_secret", length = 200)
    private String stripeClientSecret;

    @Column(name = "platform_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "cook_payout", precision = 10, scale = 2)
    private BigDecimal cookPayout;

    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Column(name = "card_brand", length = 20)
    private String cardBrand;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    @Column(name = "refund_id", length = 100)
    private String refundId;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void markAsPaid(String chargeId, LocalDateTime paidAt) {
        this.status = PaymentStatus.SUCCEEDED;
        this.stripeChargeId = chargeId;
        this.paidAt = paidAt;
    }

    public void markAsFailed(String failureCode, String failureMessage) {
        this.status = PaymentStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
    }

    public void markAsCancelled() {
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void markAsRefunded(String refundId, BigDecimal amount, String reason) {
        this.status = PaymentStatus.REFUNDED;
        this.refundId = refundId;
        this.refundAmount = amount;
        this.refundReason = reason;
        this.refundedAt = LocalDateTime.now();
    }

    public void updateStatus(PaymentStatus newStatus) {
        this.status = newStatus;
    }

    public boolean canBeRefunded() {
        return this.status == PaymentStatus.SUCCEEDED &&
                this.refundedAt == null &&
                this.paidAt != null &&
                this.paidAt.plusDays(30).isAfter(LocalDateTime.now());
    }

    public boolean canBeCancelled() {
        return this.status == PaymentStatus.PENDING ||
                this.status == PaymentStatus.REQUIRES_ACTION;
    }

    public boolean isSuccessful() {
        return this.status == PaymentStatus.SUCCEEDED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean isFinal() {
        return this.status == PaymentStatus.SUCCEEDED ||
                this.status == PaymentStatus.FAILED ||
                this.status == PaymentStatus.CANCELLED ||
                this.status == PaymentStatus.REFUNDED;
    }
}