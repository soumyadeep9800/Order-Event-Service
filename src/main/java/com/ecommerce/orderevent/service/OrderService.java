package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.MenuItemRepository;
import com.ecommerce.orderevent.repository.OrderRepository;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import com.ecommerce.orderevent.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

import static com.ecommerce.orderevent.constants.ErrorMessages.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        RestaurantRepository restaurantRepository,
                        MenuItemRepository menuItemRepository){
        this.orderRepository=orderRepository;
        this.restaurantRepository=restaurantRepository;
        this.userRepository=userRepository;
        this.menuItemRepository=menuItemRepository;
    }

    public Order placeOrder(Long userId, Long restaurantId, List<Long> menuItemIds){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException(USER_NOT_FOUND));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(()-> new ResourceNotFoundException(RESTAURANT_NOT_FOUND));

        List<MenuItem> items = menuItemRepository.findAllById(menuItemIds);
        if(items.isEmpty()) throw new ResourceNotFoundException( MENU_ITEM_NOT_FOUND + menuItemIds);
        //calculate total price
        double totalPrice = items.stream().mapToDouble(MenuItem::getPrice).sum();

        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(Long userId){
        return orderRepository.findByUserId(userId);
    }

    public void cancelOrder(Long orderId){
        orderRepository.deleteById(orderId);
    }
}
