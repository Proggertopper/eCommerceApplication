package com.ecommerce.order.services;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dtos.CartItemRequest;
import com.ecommerce.order.dtos.ProductResponse;
import com.ecommerce.order.dtos.UserResponse;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.repositories.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCartCreatesNewItemWhenProductAndUserExist() {
        CartItemRequest request = cartItemRequest("1", 2);
        when(productServiceClient.getProductByDetails("1")).thenReturn(productResponse(5));
        when(userServiceClient.getUserById("user-1")).thenReturn(new UserResponse());

        boolean added = cartService.addToCart("user-1", request);

        assertThat(added).isTrue();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCartRejectsOutOfStockProduct() {
        CartItemRequest request = cartItemRequest("1", 10);
        when(productServiceClient.getProductByDetails("1")).thenReturn(productResponse(2));

        boolean added = cartService.addToCart("user-1", request);

        assertThat(added).isFalse();
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCartUpdatesExistingItemQuantity() {
        CartItem existing = new CartItem();
        existing.setUserId("user-1");
        existing.setProductId("1");
        existing.setQuantity(1);
        existing.setPrice(new BigDecimal("1000"));

        CartItemRequest request = cartItemRequest("1", 2);
        when(productServiceClient.getProductByDetails("1")).thenReturn(productResponse(5));
        when(userServiceClient.getUserById("user-1")).thenReturn(new UserResponse());
        when(cartItemRepository.findByUserIdAndProductId("user-1", "1")).thenReturn(existing);

        boolean added = cartService.addToCart("user-1", request);

        assertThat(added).isTrue();
        assertThat(existing.getQuantity()).isEqualTo(3);
        verify(cartItemRepository).save(existing);
    }

    @Test
    void deleteItemFromCartRemovesExistingItem() {
        CartItem existing = new CartItem();
        when(cartItemRepository.findByUserIdAndProductId("user-1", "1")).thenReturn(existing);

        boolean deleted = cartService.deleteItemFromCart("user-1", "1");

        assertThat(deleted).isTrue();
        verify(cartItemRepository).delete(existing);
    }

    @Test
    void getCartItemsOfUserDelegatesToRepository() {
        when(cartItemRepository.findByUserId("user-1")).thenReturn(List.of(new CartItem()));

        List<CartItem> result = cartService.getCartItemsOfUser("user-1");

        assertThat(result).hasSize(1);
    }

    private CartItemRequest cartItemRequest(String productId, int quantity) {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    private ProductResponse productResponse(int stockQuantity) {
        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setStockQuantity(stockQuantity);
        return response;
    }
}
