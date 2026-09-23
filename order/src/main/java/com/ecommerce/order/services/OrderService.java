package com.ecommerce.order.services;


import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dtos.OrderCreatedEvent;
import com.ecommerce.order.dtos.OrderItemDTO;
import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.dtos.UserResponse;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderItem;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserServiceClient userServiceClient;
    private final StreamBridge streamBridge;
//    private final RabbitTemplate rabbitTemplate;

    public Optional<OrderResponse> createOrder(String userId) {
        // Validate for cart cartItems
        List<CartItem> cartItems = cartService.getCartItemsOfUser(userId);
        if(cartItems.isEmpty()){
            return Optional.empty();
        }
        // Validate for user
        UserResponse userResponse = userServiceClient.getUserById(userId);
        if(userResponse == null){
            return Optional.empty();
        }

        // Calculate total price
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO , BigDecimal::add);

        // create order

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);
        List<OrderItem> orderItems = cartItems.stream()
                        .map(item -> new OrderItem(
                                null ,
                                item.getProductId(),
                                item.getQuantity(),
                                item.getPrice(),
                                order
                        ))
                                .toList();



        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        // clear cart
        cartService.clearCart(userId);

        // Publish order created event
        OrderCreatedEvent event =new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getStatus(),
                mapToListOfOrderItemDTO(savedOrder.getItems()),
                savedOrder.getTotalAmount(),
                savedOrder.getCreatedAt()
        );

//        rabbitTemplate.convertAndSend("order.exchange" , "order.tracking" ,
//                event);
        streamBridge.send("createOrder-out-0" , event);

        return Optional.of(mapToOrderResponse(savedOrder));
    }

    private OrderResponse mapToOrderResponse(Order savedOrder) {
        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getItems().stream()
                        .map(orderItem -> new OrderItemDTO(
                                orderItem.getId(),
                                orderItem.getProductId(),
                                orderItem.getQuantity(),
                                orderItem.getPrice(),
                                orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()))
                        )).toList(),
                savedOrder.getCreatedAt()
        );
    }

    private List<OrderItemDTO> mapToListOfOrderItemDTO(List<OrderItem> items){
        return items.stream().map(item -> new OrderItemDTO(item.getId(),
                item.getProductId() , item.getQuantity() , item.getPrice() , item.getPrice()
                .multiply(new BigDecimal(item.getQuantity())))).collect(Collectors.toList());
    }
}
