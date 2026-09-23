package com.ecommerce.order.clients;

import com.ecommerce.order.dtos.ProductResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;


@HttpExchange("/api/products")
public interface ProductServiceClient {

    @GetExchange("/{id}")
    ProductResponse getProductByDetails(@PathVariable String id);
}

