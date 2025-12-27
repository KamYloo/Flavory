package com.flavory.deliveryservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class StuartConfig {

    @Value("${stuart.api.enabled}")
    private boolean enabled;

    @Value("${stuart.api.base-url}")
    private String baseUrl;

    @Value("${stuart.api.client-id}")
    private String clientId;

    @Value("${stuart.api.client-secret}")
    private String clientSecret;

    @Value("${stuart.api.retry.max-attempts}")
    private int retryMaxAttempts;

    @Value("${stuart.api.retry.initial-interval}")
    private long retryInitialInterval;

    @Value("${stuart.api.retry.max-interval}")
    private long retryMaxInterval;

    @Value("${stuart.webhook.secret}")
    private String webhookSecret;

    @Value("${stuart.webhook.enabled}")
    private boolean webhookEnabled;

    public boolean isStuartEnabled() {
        return enabled;
    }

    public String getOAuth2TokenUrl() {
        return baseUrl + "/oauth/token";
    }

    public String getJobsUrl() {
        return baseUrl + "/v2/jobs";
    }

    public String getJobUrl(Long jobId) {
        return baseUrl + "/v2/jobs/" + jobId;
    }
}
