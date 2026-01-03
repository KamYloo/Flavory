package com.flavory.paymentservice.repository;

import com.flavory.paymentservice.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByOrderId(Long orderId);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    Optional<Payment> findByOrderId(Long orderId);
    Page<Payment> findByCustomerId(String customerId, Pageable pageable);
    Page<Payment> findByCookId(String cookId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :expiryTime")
    List<Payment> findExpiredPendingPayments(@Param("expiryTime") LocalDateTime expiryTime);
}
