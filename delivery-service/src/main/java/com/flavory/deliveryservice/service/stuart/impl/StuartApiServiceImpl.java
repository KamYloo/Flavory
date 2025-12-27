package com.flavory.deliveryservice.service.stuart.impl;

import com.flavory.deliveryservice.client.StuartApiClient;
import com.flavory.deliveryservice.config.StuartConfig;
import com.flavory.deliveryservice.dto.request.StuartJobRequest;
import com.flavory.deliveryservice.dto.response.StuartAuthResponse;
import com.flavory.deliveryservice.dto.response.StuartJobResponse;
import com.flavory.deliveryservice.exception.StuartApiException;
import com.flavory.deliveryservice.service.stuart.StuartApiService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StuartApiServiceImpl implements StuartApiService {

    private final StuartApiClient stuartApiClient;
    private final StuartConfig stuartConfig;

    private String cachedAccessToken;
    private LocalDateTime tokenExpiryTime;

    @Override
    public StuartJobResponse createJob(StuartJobRequest request) {
        if (!stuartConfig.isStuartEnabled()) {
            throw new StuartApiException("Stuart API is disabled");
        }

        try {
            String token = getAccessToken();
            return stuartApiClient.createJob(
                    "Bearer " + token,
                    request
            );

        } catch (FeignException e) {
            throw new StuartApiException("Failed to create Stuart job: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new StuartApiException("Unexpected error creating Stuart job", e);
        }
    }

    @Override
    public String getAccessToken() {
        if (cachedAccessToken != null && tokenExpiryTime != null &&
                LocalDateTime.now().isBefore(tokenExpiryTime)) {

            return cachedAccessToken;
        }

        try {
            StuartAuthResponse authResponse = stuartApiClient.authenticate(
                    "client_credentials",
                    stuartConfig.getClientId(),
                    stuartConfig.getClientSecret()
            );

            cachedAccessToken = authResponse.getAccessToken();

            int expiresIn = authResponse.getExpiresIn() != null ?
                    authResponse.getExpiresIn() : 3600;
            tokenExpiryTime = LocalDateTime.now().plusSeconds(expiresIn - 300);

            return cachedAccessToken;

        } catch (FeignException e) {
            throw new StuartApiException("Stuart authentication failed: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new StuartApiException("Unexpected error during Stuart authentication", e);
        }
    }
}
