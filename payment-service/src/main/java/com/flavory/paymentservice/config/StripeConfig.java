package com.flavory.paymentservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stripe.api")
@Getter
@Setter
@Slf4j
public class StripeConfig {

    private String key;
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.key;
    }
}
