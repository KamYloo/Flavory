package com.flavory.deliveryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavory.deliveryservice.config.StuartConfig;
import com.flavory.deliveryservice.dto.external.StuartWebhookEvent;
import com.flavory.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/webhooks/stuart")
@RequiredArgsConstructor
public class StuartWebhookController {
    private final DeliveryService deliveryService;
    private final StuartConfig stuartConfig;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Void> handleStuartWebhook(@RequestBody String rawPayload) {
        try {
            StuartWebhookEvent webhookEvent = objectMapper.readValue(rawPayload, StuartWebhookEvent.class);
            processWebhook(webhookEvent);
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok().build();
    }

    private void processWebhook(StuartWebhookEvent webhookEvent) {
        if (webhookEvent.getDetails() == null || webhookEvent.getDetails().getPackageData() == null) {
            return;
        }

        Long stuartIdLong = webhookEvent.getDetails().getPackageData().getId();
        String stuartJobId = stuartIdLong != null ? stuartIdLong.toString() : null;

        String status = webhookEvent.getTopic();


        if (stuartJobId == null) {
            return;
        }

        String courierName = null;
        String courierPhone = null;

        if (webhookEvent.getDetails().getCourier() != null) {
            courierName = webhookEvent.getDetails().getCourier().getName();
            courierPhone = webhookEvent.getDetails().getCourier().getPhone();
        }

        String trackingUrl = webhookEvent.getDetails().getPackageData().getEndCustomerTrackingUrl();

        deliveryService.updateDeliveryStatus(stuartJobId, status, courierName, courierPhone, trackingUrl);
    }

    private boolean validateSignature(String payload, String signature) {
        String secret = stuartConfig.getWebhookSecret();

        if (secret == null || secret.isBlank()) {
            return false;
        }

        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);

            byte[] hashBytes = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String computedSignature = hexString.toString();

            return computedSignature.equals(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }
}
