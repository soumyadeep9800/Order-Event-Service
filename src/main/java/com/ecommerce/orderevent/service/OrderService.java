package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.OrderRequestDto;
import com.ecommerce.orderevent.dtos.OrderResponseDto;
import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.models.OrderEvent;
import com.ecommerce.orderevent.repository.MenuItemRepository;
import com.ecommerce.orderevent.repository.OrderRepository;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import com.ecommerce.orderevent.repository.UserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import static com.ecommerce.orderevent.constants.ErrorMessages.*;

@Service
public class OrderService {

    private static final String ORDER_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        RestaurantRepository restaurantRepository,
                        MenuItemRepository menuItemRepository,
                        KafkaTemplate<String, OrderEvent> kafkaTemplate){
        this.orderRepository=orderRepository;
        this.restaurantRepository=restaurantRepository;
        this.userRepository=userRepository;
        this.menuItemRepository=menuItemRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderResponseDto placeOrder(OrderRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + requestDto.getUserId()));

        Restaurant restaurant = restaurantRepository.findById(requestDto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND + requestDto.getRestaurantId()));

        List<MenuItem> items = menuItemRepository.findAllById(requestDto.getMenuItemIds());
        if (items.isEmpty()) {
            throw new ResourceNotFoundException(MENU_ITEM_NOT_FOUND + requestDto.getMenuItemIds());
        }

        double totalPrice = items.stream()
                .mapToDouble(MenuItem::getPrice)
                .sum();
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setItems(items);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setTotalPrice(totalPrice);

        // maintain both sides of the relationship
        if (user.getOrders() == null) {
            user.setOrders(new ArrayList<>());
        }
        user.getOrders().add(order);
        userRepository.save(user);

        Order savedOrder = orderRepository.save(order);

        OrderEvent event = new OrderEvent(
                savedOrder.getId(),
                user.getId(),
                restaurant.getId(),
                items.stream().map(MenuItem::getId).toList(),
                savedOrder.getStatus()
        );
        kafkaTemplate.send(ORDER_TOPIC, event);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    public Order getOrderDetails(Long id){
        return orderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(ORDER_ITEM_NOT_FOUND + id));
    }

    public String getOrderStatus(Long id) {
        return orderRepository.findById(id)
                .map(Order::getStatus) // ✅ only return status
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_ITEM_NOT_FOUND + id));
    }

    public List<Order> getOrdersByRestaurant(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(()-> new ResourceNotFoundException(RESTAURANT_NOT_FOUND + restaurantId));
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public OrderResponseDto updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new ResourceNotFoundException(ORDER_ITEM_NOT_FOUND + orderId));
        order.setStatus(status);
        Order saveOrder = orderRepository.save(order);
        return OrderResponseDto.fromEntity(saveOrder);
    }

    public void cancelOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException(ORDER_ITEM_NOT_FOUND + orderId);
        }
        orderRepository.deleteById(orderId);
    }
}
