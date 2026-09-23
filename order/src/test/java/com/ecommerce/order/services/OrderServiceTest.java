package com.ecommerce.order.services;

import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.dtos.UserResponse;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderPersistsOrderClearsCartAndPublishesEvent() {
        when(cartService.getCartItemsOfUser("user-1")).thenReturn(List.of(cartItem("1", 2, "10.00")));
        when(userServiceClient.getUserById("user-1")).thenReturn(new UserResponse());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        Optional<OrderResponse> result = orderService.createOrder("user-1");

        assertThat(result).isPresent();
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(cartService).clearCart("user-1");
        verify(streamBridge).send(eq("createOrder-out-0"), any());
    }

    @Test
    void createOrderReturnsEmptyWhenCartIsEmpty() {
        when(cartService.getCartItemsOfUser("user-1")).thenReturn(List.of());

        Optional<OrderResponse> result = orderService.createOrder("user-1");

        assertThat(result).isEmpty();
        verify(orderRepository, never()).save(any(Order.class));
        verify(streamBridge, never()).send(any(), any());
    }

    @Test
    void createOrderReturnsEmptyWhenUserDoesNotExist() {
        when(cartService.getCartItemsOfUser("user-1")).thenReturn(List.of(cartItem("1", 1, "10.00")));
        when(userServiceClient.getUserById("user-1")).thenReturn(null);

        Optional<OrderResponse> result = orderService.createOrder("user-1");

        assertThat(result).isEmpty();
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartService, never()).clearCart("user-1");
    }

    private CartItem cartItem(String productId, int quantity, String price) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(new BigDecimal(price));
        return item;
    }
}
