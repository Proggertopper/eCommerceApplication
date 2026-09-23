package com.ecommerce.order.clients;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ProductServiceClientConfig {

    @Bean
    public ProductServiceClient productClient(@Qualifier("myCustomloadBalancedRestClientBuilder") RestClient.Builder builder){
        RestClient client =builder.baseUrl("http://PRODUCT-SERVICE")
                .defaultStatusHandler(status -> status.value() == 404, (request, response) -> {})
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(client);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(ProductServiceClient.class);
    }
}
