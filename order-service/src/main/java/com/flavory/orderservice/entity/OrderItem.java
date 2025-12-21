package com.flavory.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, name = "dish_id")
    private Long dishId;

    @Column(nullable = false, name = "dish_name", length = 200)
    private String dishName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "dish_image_url", length = 500)
    private String dishImageUrl;

    @Column(nullable = false, name = "item_total", precision = 10, scale = 2)
    private BigDecimal itemTotal;

    @PrePersist
    @PreUpdate
    public void calculateItemTotal() {
        if (this.price != null && this.quantity != null) {
            this.itemTotal = this.price.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}
