package com.ecommerce.order.clients;

import com.ecommerce.order.dtos.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/users")
public interface UserServiceClient {

    @GetExchange("/{id}")
    UserResponse getUserById(@PathVariable String id);
}
