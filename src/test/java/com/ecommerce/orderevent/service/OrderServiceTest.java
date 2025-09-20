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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static com.ecommerce.orderevent.constants.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private User user;
    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp(){
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("John@example.com");
        user.setOrders(new ArrayList<>());

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");

        menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setName("Pizza");
        menuItem.setPrice(10.0);
    }

    @Test
    void testPlaceOrder_Success () {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setUserId(1L);
        requestDto.setRestaurantId(1L);
        requestDto.setMenuItemIds(List.of(1L));

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
        when(kafkaTemplate.send(anyString(), any(OrderEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        OrderResponseDto result = orderService.placeOrder(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(10.0, result.getTotalPrice());
        assertEquals("PLACED", result.getStatus());
        assertEquals("Test User", result.getUser().getName());
        assertEquals("Test Restaurant", result.getRestaurant().getName());
        assertEquals(1, result.getItems().size());

        // Verify repository and kafka calls
        verify(userRepository).findById(1L);
        verify(restaurantRepository).findById(1L);
        verify(menuItemRepository).findAllById(List.of(1L));
        verify(userRepository).save(user);
        verify(orderRepository).save(any(Order.class));
        verify(kafkaTemplate).send(eq("order-events"), any(OrderEvent.class));
    }


    @Test
    void testPlaceOrder_UserNotFound() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setUserId(1L);
        requestDto.setRestaurantId(1L);
        requestDto.setMenuItemIds(List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(requestDto));
        assertEquals(USER_NOT_FOUND + 1, ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_RestaurantNotFound() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setUserId(1L);
        requestDto.setRestaurantId(1L);
        requestDto.setMenuItemIds(List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(requestDto));
        assertEquals(RESTAURANT_NOT_FOUND + 1, ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_MenuItemsNotFound() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setUserId(1L);
        requestDto.setRestaurantId(1L);
        requestDto.setMenuItemIds(List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findAllById(List.of(1L))).thenReturn(Collections.emptyList());

        Exception ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(requestDto));
        assertTrue(ex.getMessage().contains(MENU_ITEM_NOT_FOUND));
        verify(orderRepository, never()).save(any(Order.class));
    }


    @Test
    void testGetOrderDetails_Success() {
        Order order = new Order();
        order.setId(300L);
        order.setStatus("PLACED");

        when(orderRepository.findById(300L)).thenReturn(Optional.of(order));
        Order result = orderService.getOrderDetails(300L);

        assertNotNull(result);
        assertEquals(300L, result.getId());
        assertEquals("PLACED", result.getStatus());
        verify(orderRepository, times(1)).findById(300L);
    }

    @Test
    void testGetOrderDetails_NotFound() {
        when(orderRepository.findById(300L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderDetails(300L));
        assertEquals(ORDER_ITEM_NOT_FOUND + 300L, ex.getMessage());
        verify(orderRepository, times(1)).findById(300L);
    }

    @Test
    void testGetOrdersByRestaurant_Success() {
        Order order = new Order();
        order.setId(400L);
        order.setRestaurant(restaurant);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(orderRepository.findByRestaurantId(1L)).thenReturn(List.of(order));
        List<Order> result = orderService.getOrdersByRestaurant(1L);
        assertEquals(1, result.size());
        assertEquals(400L, result.get(0).getId());
        verify(restaurantRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).findByRestaurantId(1L);
    }

    @Test
    void testGetOrdersByRestaurant_RestaurantNotFound() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrdersByRestaurant(1L));
        assertEquals(RESTAURANT_NOT_FOUND + 1L, ex.getMessage());
        verify(orderRepository, never()).findByRestaurantId(anyLong());
    }

    @Test
    void testUpdateOrderStatus_Success() {
        Order order = new Order();
        order.setId(500L);
        order.setStatus("PLACED");

        when(orderRepository.findById(500L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponseDto result = orderService.updateOrderStatus(500L, "DELIVERED");

        assertEquals("DELIVERED", result.getStatus());
        assertEquals(500L, result.getId());
        verify(orderRepository, times(1)).findById(500L);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdateOrderStatus_NotFound() {
        when(orderRepository.findById(500L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(500L, "DELIVERED"));
        assertEquals(ORDER_ITEM_NOT_FOUND + 500L, ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetOrderStatus_OrderFound() {
        // Arrange
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus("PLACED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        String status = orderService.getOrderStatus(orderId);
        // Assert
        assertEquals("PLACED", status);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void testGetOrderStatus_OrderNotFound() {
        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderStatus(orderId)
        );
        assertEquals(ORDER_ITEM_NOT_FOUND + orderId, exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
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

    @Test
    void testUpdateOrderStatusForRestaurant_Success() {
        // Arrange
        Order order = new Order();
        order.setId(500L);
        order.setStatus("PLACED");
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setItems(List.of(menuItem));

        when(orderRepository.findById(500L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(kafkaTemplate.send(anyString(), any(OrderEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        String result = orderService.updateOrderStatusForRestaurant(500L, "ACCEPTED");
        // Assert
        assertEquals("ACCEPTED", result);
        assertEquals("ACCEPTED", order.getStatus()); // order object updated
        verify(orderRepository, times(1)).findById(500L);
        verify(orderRepository, times(1)).save(order);
        verify(kafkaTemplate, times(1)).send(eq("order-events"), any(OrderEvent.class));
    }

    @Test
    void testUpdateOrderStatusForRestaurant_OrderNotFound() {
        when(orderRepository.findById(500L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrderStatusForRestaurant(500L, "ACCEPTED"));
        assertEquals(ORDER_ITEM_NOT_FOUND + 500L, ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), any(OrderEvent.class));
    }

    @Test
    void testUpdateOrderStatusForRestaurant_InvalidState() {
        Order order = new Order();
        order.setId(501L);
        order.setStatus("DELIVERED"); // Not "PLACED"
        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> orderService.updateOrderStatusForRestaurant(501L, "ACCEPTED"));
        assertEquals("Order cannot be modified at this stage.", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), any(OrderEvent.class));
    }

}
