package com.ecommerce.order.controllers;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @GetMapping("/message")
    @RateLimiter(name = "rateBreaker" , fallbackMethod = "getMessageFallback")
    public String getMessage(){
        return "hello";
    }

    public String getMessageFallback(Exception e){
        return "Hello Fallback";
    }
}
