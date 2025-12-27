package com.flavory.deliveryservice.client;

import com.flavory.deliveryservice.dto.request.StuartJobRequest;
import com.flavory.deliveryservice.dto.response.StuartAuthResponse;
import com.flavory.deliveryservice.dto.response.StuartJobResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "stuart-api",
        url = "${stuart.api.base-url}",
        configuration = StuartApiClient.StuartFeignConfig.class
)
public interface  StuartApiClient {

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    StuartAuthResponse authenticate(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret
    );
    @PostMapping("/v2/jobs")
    StuartJobResponse createJob(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody StuartJobRequest request
    );

    class StuartFeignConfig {
    }
}
