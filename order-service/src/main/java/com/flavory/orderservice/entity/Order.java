package com.flavory.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_cook_id", columnList = "cook_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "customer_id")
    private String customerId;

    @Column(nullable = false, name = "cook_id")
    private String cookId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "cook_name")
    private String cookName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, name = "delivery_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(nullable = false, name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "pickup_street")),
            @AttributeOverride(name = "city", column = @Column(name = "pickup_city")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "pickup_postal_code")),
            @AttributeOverride(name = "apartmentNumber", column = @Column(name = "pickup_apartment_number")),
            @AttributeOverride(name = "floor", column = @Column(name = "pickup_floor")),
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "pickup_phone_number")),
            @AttributeOverride(name = "deliveryInstructions", column = @Column(name = "pickup_instructions")),
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude"))
    })
    private DeliveryAddress pickupAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "dropoff_street")),
            @AttributeOverride(name = "city", column = @Column(name = "dropoff_city")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "dropoff_postal_code")),
            @AttributeOverride(name = "apartmentNumber", column = @Column(name = "dropoff_apartment_number")),
            @AttributeOverride(name = "floor", column = @Column(name = "dropoff_floor")),
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "dropoff_phone_number")),
            @AttributeOverride(name = "deliveryInstructions", column = @Column(name = "dropoff_instructions")),
            @AttributeOverride(name = "latitude", column = @Column(name = "dropoff_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "dropoff_longitude"))
    })
    private DeliveryAddress deliveryAddress;

    @Column(name = "customer_notes", length = 500)
    private String customerNotes;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "glovo_order_id", length = 100)
    private String glovoOrderId;

    @Column(name = "glovo_tracking_url", length = 500)
    private String glovoTrackingUrl;

    @Column(name = "dish_rating", precision = 3, scale = 2)
    private BigDecimal dishRating;

    @Column(name = "rated_dish_id")
    private Long ratedDishId;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(OrderItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = this.subtotal.add(this.deliveryFee);
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING ||
                this.status == OrderStatus.PAID;
    }

    public boolean canBeRated() {
        return this.status == OrderStatus.DELIVERED;
    }

    @Getter
    public enum OrderStatus {
        PENDING("Oczekujące"),
        PAID("Opłacone"),
        CONFIRMED("Potwierdzone przez kucharza"),
        PREPARING("W przygotowaniu"),
        READY("Gotowe do odbioru"),
        IN_DELIVERY("W dostawie"),
        DELIVERED("Dostarczone"),
        CANCELLED("Anulowane"),
        FAILED("Nieudane");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }
    }
}
