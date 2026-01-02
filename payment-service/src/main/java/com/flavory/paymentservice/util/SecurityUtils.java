package com.flavory.paymentservice.util;

import com.flavory.paymentservice.dto.response.PaymentResponse;
import org.springframework.security.oauth2.jwt.Jwt;


public class SecurityUtils {

    private SecurityUtils() {
    }

    public static boolean hasAccessToPayment(PaymentResponse payment, String userId) {
        return payment.getCustomerId().equals(userId) ||
                payment.getCookId().equals(userId);
    }
}

