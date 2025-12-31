package com.flavory.deliveryservice.entity;

import com.flavory.deliveryservice.exception.InvalidDeliveryStatusException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_stuart_job_id", columnList = "stuart_job_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "order_id")
    private Long orderId;

    @Column(nullable = false, name = "customer_id")
    private String customerId;

    @Column(nullable = false, name = "cook_id")
    private String cookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "stuart_job_id", length = 100)
    private String stuartJobId;

    @Column(name = "tracking_url", length = 500)
    private String trackingUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "pickup_street")),
            @AttributeOverride(name = "city", column = @Column(name = "pickup_city")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "pickup_postal_code")),
            @AttributeOverride(name = "apartmentNumber", column = @Column(name = "pickup_apartment")),
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "pickup_phone")),
            @AttributeOverride(name = "contactName", column = @Column(name = "pickup_contact_name")),
            @AttributeOverride(name = "instructions", column = @Column(name = "pickup_instructions")),
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude"))
    })
    private DeliveryAddress pickupAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "dropoff_street")),
            @AttributeOverride(name = "city", column = @Column(name = "dropoff_city")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "dropoff_postal_code")),
            @AttributeOverride(name = "apartmentNumber", column = @Column(name = "dropoff_apartment")),
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "dropoff_phone")),
            @AttributeOverride(name = "contactName", column = @Column(name = "dropoff_contact_name")),
            @AttributeOverride(name = "instructions", column = @Column(name = "dropoff_instructions")),
            @AttributeOverride(name = "latitude", column = @Column(name = "dropoff_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "dropoff_longitude"))
    })
    private DeliveryAddress dropoffAddress;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_pickup_time")
    private LocalDateTime actualPickupTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "courier_name", length = 100)
    private String courierName;

    @Column(name = "courier_phone", length = 20)
    private String courierPhone;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void updateStatus(DeliveryStatus newStatus) {
        if (this.status == newStatus) {
            return;
        }

        validateStatusTransition(this.status, newStatus);
        this.status = newStatus;
    }

    public boolean canBeCancelled() {
        return this.status == DeliveryStatus.PENDING ||
                this.status == DeliveryStatus.SCHEDULED;
    }

    public boolean isCompleted() {
        return this.status == DeliveryStatus.DELIVERED ||
                this.status == DeliveryStatus.CANCELLED;
    }

    @Getter
    public enum DeliveryStatus {
        PENDING("Oczekujące"),
        SCHEDULED("Zaplanowane"),
        COURIER_ASSIGNED("Kurier przydzielony"),
        PICKED_UP("Odebrane"),
        IN_TRANSIT("W drodze"),
        DELIVERED("Dostarczone"),
        CANCELLED("Anulowane"),
        FAILED("Nieudane");

        private final String displayName;

        DeliveryStatus(String displayName) {
            this.displayName = displayName;
        }
    }

    private void validateStatusTransition(DeliveryStatus from, DeliveryStatus to) {
        boolean isValid = switch (from) {
            case PENDING -> to == DeliveryStatus.SCHEDULED ||
                    to == DeliveryStatus.CANCELLED ||
                    to == DeliveryStatus.FAILED;

            case SCHEDULED -> to == DeliveryStatus.COURIER_ASSIGNED ||
                    to == DeliveryStatus.CANCELLED ||
                    to == DeliveryStatus.FAILED;

            case COURIER_ASSIGNED -> to == DeliveryStatus.PICKED_UP ||
                    to == DeliveryStatus.IN_TRANSIT ||
                    to == DeliveryStatus.CANCELLED ||
                    to == DeliveryStatus.FAILED;

            case PICKED_UP -> to == DeliveryStatus.IN_TRANSIT ||
                    to == DeliveryStatus.CANCELLED ||
                    to == DeliveryStatus.FAILED;

            case IN_TRANSIT -> to == DeliveryStatus.DELIVERED ||
                    to == DeliveryStatus.CANCELLED ||
                    to == DeliveryStatus.FAILED;

            case DELIVERED, CANCELLED, FAILED -> false;
        };

        if (!isValid) {
            throw new InvalidDeliveryStatusException(from, to);
        }
    }
}
