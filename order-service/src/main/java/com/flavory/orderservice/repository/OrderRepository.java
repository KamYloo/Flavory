package com.flavory.orderservice.repository;

import com.flavory.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByCookId(String cookId, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByCookIdAndStatus(String cookId, Order.OrderStatus status, Pageable pageable);
}
