package com.flavory.paymentservice.controller;

import com.flavory.paymentservice.config.StripeConfig;
import com.flavory.paymentservice.exception.WebhookVerificationException;
import com.flavory.paymentservice.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentService paymentService;
    private final StripeConfig stripeConfig;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader
    ) {
        log.info("Otrzymano webhook od Stripe");

        Event event;
        try {
            event = Webhook.constructEvent(
                    payload,
                    signatureHeader,
                    stripeConfig.getWebhookSecret()
            );

        } catch (SignatureVerificationException e) {
            throw new WebhookVerificationException("Nieprawidłowa sygnatura", e);
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Nieprawidłowe dane zdarzenia");
        }

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded((PaymentIntent) stripeObject);
                    break;

                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed((PaymentIntent) stripeObject);
                    break;

                case "payment_intent.canceled":
                    handlePaymentIntentCanceled((PaymentIntent) stripeObject);
                    break;

                case "payment_intent.requires_action":
                    handlePaymentIntentRequiresAction((PaymentIntent) stripeObject);
                    break;

                case "payment_intent.processing":
                    handlePaymentIntentProcessing((PaymentIntent) stripeObject);
                    break;

                case "charge.refunded":
                    break;

                default:
            }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook");
        }
    }

    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        paymentService.confirmPayment(paymentIntent.getId());
    }

    private void handlePaymentIntentFailed(PaymentIntent paymentIntent) {
        String failureCode = null;
        String failureMessage = "Płatność nie powiodła się";

        if (paymentIntent.getLastPaymentError() != null) {
            failureCode = paymentIntent.getLastPaymentError().getCode();
            failureMessage = paymentIntent.getLastPaymentError().getMessage();
        }
        paymentService.markPaymentAsFailed(paymentIntent.getId(), failureCode, failureMessage);
    }

    private void handlePaymentIntentCanceled(PaymentIntent paymentIntent) {
        paymentService.markPaymentAsFailed(
                paymentIntent.getId(),
                "canceled",
                "Płatność została anulowana"
        );
    }

    private void handlePaymentIntentRequiresAction(PaymentIntent paymentIntent) {
        paymentService.confirmPayment(paymentIntent.getId());
    }

    private void handlePaymentIntentProcessing(PaymentIntent paymentIntent) {
        paymentService.confirmPayment(paymentIntent.getId());
    }
}