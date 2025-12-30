package com.flavory.deliveryservice.client;

import com.flavory.deliveryservice.dto.request.StuartJobRequest;
import com.flavory.deliveryservice.dto.external.StuartAuthResponse;
import com.flavory.deliveryservice.dto.external.StuartJobResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/v2/jobs/{jobId}")
    StuartJobResponse getJob(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("jobId") Long jobId
    );

    @DeleteMapping("/v2/jobs/{jobId}")
    void cancelJob(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("jobId") Long jobId
    );

    class StuartFeignConfig {
    }
}
