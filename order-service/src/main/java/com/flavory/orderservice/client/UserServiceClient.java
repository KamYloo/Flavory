package com.flavory.orderservice.client;

import com.flavory.orderservice.dto.response.AddressDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/by-auth0/{auth0Id}/default")
    AddressDto getDefaultAddressByAuth0Id(
            @PathVariable("auth0Id") String auth0Id
    );
}
