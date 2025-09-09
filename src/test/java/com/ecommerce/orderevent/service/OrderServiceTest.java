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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ecommerce.orderevent.constants.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private MenuItemRepository menuItemRepository;

    private User user;
    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp(){
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("John@example.com");

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Pizza Place");

        menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setName("Pizza");
        menuItem.setPrice(10.0);
    }

    @Test
    void testPlaceOrder_Success(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findAllById(List.of(1L))).thenReturn(List.of(menuItem));

        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setUser(user);
        savedOrder.setRestaurant(restaurant);
        savedOrder.setItems(List.of(menuItem));
        savedOrder.setTotalPrice(10.0);
        savedOrder.setStatus("PLACED");

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        Order result = orderService.placeOrder(1L, 1L, List.of(1L));

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(10.0, result.getTotalPrice());
        assertEquals("PLACED", result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                orderService.placeOrder(1L, 1L, List.of(1L)));

        assertEquals(USER_NOT_FOUND + 1, ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_RestaurantNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                orderService.placeOrder(1L, 1L, List.of(1L)));

        assertEquals(RESTAURANT_NOT_FOUND + 1, ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_MenuItemsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findAllById(List.of(1L))).thenReturn(Collections.emptyList());

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                orderService.placeOrder(1L, 1L, List.of(1L)));

        assertTrue(ex.getMessage().contains(MENU_ITEM_NOT_FOUND));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetUserOrders(){
        Order order = new Order();
        order.setId(101L);
        order.setUser(user);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));
        List<Order> result = orderService.getUserOrders(1L);
        assertEquals(1,result.size());
        assertEquals(101L, result.get(0).getId());
        verify(userRepository, times(1)).existsById(1L);
        verify(orderRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testCancelOrder() {
        Long orderId = 200L;
        when(orderRepository.existsById(orderId)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(orderId);
        orderService.cancelOrder(orderId);
        verify(orderRepository, times(1)).existsById(orderId);
        verify(orderRepository, times(1)).deleteById(orderId);
    }
}
