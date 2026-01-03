package com.flavory.paymentservice.service;

public interface PaymentCleanupService {
    void cancelExpiredPendingPayments();
}
