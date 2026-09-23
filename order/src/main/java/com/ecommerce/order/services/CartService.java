package com.ecommerce.order.services;


import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dtos.CartItemRequest;
import com.ecommerce.order.dtos.ProductResponse;
import com.ecommerce.order.dtos.UserResponse;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.repositories.CartItemRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;
    int attempt = 0;

//    @CircuitBreaker(name = "productService" , fallbackMethod = "addToCartFallback")
@Retry(name = "retryBreaker" , fallbackMethod = "addToCartFallback")
    public boolean addToCart(String userId , CartItemRequest cartItemRequest){
    log.debug("Add to cart attempt count: {}", ++attempt);
        ProductResponse productResponse = productServiceClient.getProductByDetails(cartItemRequest.getProductId());

        if (productResponse == null || productResponse.getStockQuantity() < cartItemRequest.getQuantity())
            return false;

        UserResponse userResponse = userServiceClient.getUserById(userId);
        if(userResponse == null)
            return false;

        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId , cartItemRequest.getProductId());

        if(existingCartItem  != null){
            // Updating Quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + cartItemRequest.getQuantity());
            existingCartItem.setPrice(new BigDecimal(1000));
            cartItemRepository.save(existingCartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(cartItemRequest.getProductId());
            cartItem.setPrice(new BigDecimal(1000));
            cartItem.setQuantity(cartItemRequest.getQuantity());
            cartItemRepository.save(cartItem);
        }
        return true;
    }

    public boolean addToCartFallback(String userId, CartItemRequest cartItemRequest , Exception exception){
        log.warn("Could not add product {} to cart for user {}", cartItemRequest.getProductId(), userId, exception);
        return false;
    }

    public boolean deleteItemFromCart(String userId, String productId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId , productId);
          if(cartItem != null){
              cartItemRepository.delete(cartItem);
              return true;
          }
        return false;
    }


    public List<CartItem> getCartItemsOfUser(String userId) {
           return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
