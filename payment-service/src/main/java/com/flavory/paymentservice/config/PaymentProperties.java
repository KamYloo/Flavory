package com.flavory.paymentservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentProperties {

    private Processing processing = new Processing();
    private Fees fees = new Fees();

    @Getter
    @Setter
    public static class Processing {
        private Long timeoutMillis = 30000L;
        private Retry retry = new Retry();

        @Getter
        @Setter
        public static class Retry {
            private Integer maxAttempts = 3;
            private Long delayMillis = 1000L;
        }
    }

    @Getter
    @Setter
    public static class Fees {
        private BigDecimal platformPercentage = new BigDecimal("10.0");
        private BigDecimal minPlatformFee = new BigDecimal("2.00");
        private BigDecimal maxPlatformFee = new BigDecimal("50.00");
    }
}
