package com.ecommerce.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class GatewayConfig {

    @Value("${eureka.url:http://localhost:8761}")
    private String eurekaUrl;

    @Bean
    public RedisRateLimiter redisRateLimiter(){
        return new RedisRateLimiter(10 , 20 ,1 );
    }

    @Bean
    public KeyResolver hostNameKeyResolver(){
        return  exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
        );
    }

    @Bean
    public RouteLocator customRouteLocatorInJavaCode(RouteLocatorBuilder builder){
        return builder.routes()
                .route("product-service" , f-> f.path("/api/products/**")
                        .filters(fn -> fn
                                .requestRateLimiter(config ->config
                                .setRateLimiter(redisRateLimiter()).setKeyResolver(hostNameKeyResolver()))
                                .circuitBreaker(config ->
                                config
                                        .setName("ecomBreaker")
                                        .setFallbackUri("forward:/fallback/products")))
                                .uri("lb://PRODUCT-SERVICE")

                ).route("user-service" , f-> f.path("/api/users/**")
                        .uri("lb://USER-SERVICE"))
                .route("order-service" , f-> f.path("/api/orders/**", "/api/cart/**")
                        .uri("lb://ORDER-SERVICE"))
                .route("product-service-openapi" , f -> f.path("/aggregate/product-service/v3/api-docs")
                        .filters(fn -> fn.setPath("/v3/api-docs"))
                        .uri("lb://PRODUCT-SERVICE"))
                .route("user-service-openapi" , f -> f.path("/aggregate/user-service/v3/api-docs")
                        .filters(fn -> fn.setPath("/v3/api-docs"))
                        .uri("lb://USER-SERVICE"))
                .route("order-service-openapi" , f -> f.path("/aggregate/order-service/v3/api-docs")
                        .filters(fn -> fn.setPath("/v3/api-docs"))
                        .uri("lb://ORDER-SERVICE"))
                .route("eureka" , f -> f.path("/eureka/main").filters(fn -> fn.rewritePath("/eureka/main", "/")).uri(eurekaUrl))
                .route("eureka-static" , f -> f.path("/eureka/**").uri(eurekaUrl))
                .build();
    }
@Bean
public RouteLocator customRouteLocatorWithoutAPIPrefix(RouteLocatorBuilder builder){
    return builder.routes()
            .route("product-service" , f-> f.path("/products/**")
                    .filters(fn -> fn.rewritePath("/products(?<segment>/?.*)" , "/api/products${segment}"))
                    .uri("lb://PRODUCT-SERVICE")
            ).route("user-service" , f-> f.path("/users/**")
                    .filters(fn -> fn.rewritePath("/users(?<segment>/?.*)" , "/api/users${segment}"))
                    .uri("lb://USER-SERVICE"))
            .route("order-service" , f-> f.path("/orders/**", "/cart/**")
                    .filters(fn -> fn.rewritePath("/(?<segment>.*)" , "/api/${segment}"))
                    .uri("lb://ORDER-SERVICE"))
            .route("eureka" , f -> f.path("/eureka/main").filters(fn -> fn.rewritePath("/eureka/main", "/")).uri(eurekaUrl))
            .route("eureka-static" , f -> f.path("/eureka/**").uri(eurekaUrl))
            .build();
}
}
