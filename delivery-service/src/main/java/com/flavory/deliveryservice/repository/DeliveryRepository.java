package com.flavory.deliveryservice.repository;

import com.flavory.deliveryservice.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    boolean existsByOrderId(Long orderId);
    Optional<Delivery> findByOrderId(Long orderId);
    Page<Delivery> findByCustomerId(String customerId, Pageable pageable);
    Page<Delivery> findByCookId(String cookId, Pageable pageable);
}
