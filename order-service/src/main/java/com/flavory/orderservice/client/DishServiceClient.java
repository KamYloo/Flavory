package com.flavory.orderservice.client;

import com.flavory.orderservice.dto.response.DishDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "dish-service")
public interface DishServiceClient {

    @GetMapping("/api/dishes/batch")
    List<DishDto> getDishesByIds(@RequestParam("ids") List<Long> ids);
}
