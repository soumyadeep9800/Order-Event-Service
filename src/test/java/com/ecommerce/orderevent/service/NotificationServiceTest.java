package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.models.OrderEvent;
import com.ecommerce.orderevent.repository.MenuItemRepository;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import com.ecommerce.orderevent.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Restaurant restaurant;
    private MenuItem item1;
    private MenuItem item2;
    private OrderEvent event;

    @BeforeEach
    void setUp() {
        // Setup User
        user = new User();
        user.setId(1L);
        user.setName("Soumyadeep");
        user.setEmail("soumyadeep@example.com");

        // Setup Restaurant
        restaurant = new Restaurant();
        restaurant.setId(10L);
        restaurant.setName("Pizza Palace");
        restaurant.setEmail("pizza@example.com");

        // Setup Menu Items
        item1 = new MenuItem();
        item1.setId(100L);
        item1.setName("Margherita Pizza");
        item1.setPrice(200.0);

        item2 = new MenuItem();
        item2.setId(101L);
        item2.setName("Garlic Bread");
        item2.setPrice(100.0);

        // Setup OrderEvent
        event = new OrderEvent();
        event.setOrderId(999L);
        event.setUserId(user.getId());
        event.setRestaurantId(restaurant.getId());
        event.setMenuItemIds(List.of(item1.getId(), item2.getId()));
    }

    @Test
    void testProcessNotification_Placed() {
        event.setStatus("PLACED");

        when(menuItemRepository.findAllById(event.getMenuItemIds())).thenReturn(List.of(item1, item2));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(restaurant.getId())).thenReturn(Optional.of(restaurant));

        notificationService.processNotification(event);

        verify(emailService, times(1))
                .sendEmail(eq(restaurant.getEmail()), contains("New Order Received"), anyString());
        verify(emailService, times(1))
                .sendEmail(eq(user.getEmail()), contains("Order PLACED"), anyString());
    }

    @Test
    void testProcessNotification_Accepted() {
        event.setStatus("ACCEPTED");

        when(menuItemRepository.findAllById(event.getMenuItemIds())).thenReturn(List.of(item1, item2));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(restaurant.getId())).thenReturn(Optional.of(restaurant));

        notificationService.processNotification(event);

        // Fixed subject check
        verify(emailService, times(1))
                .sendEmail(eq(user.getEmail()), contains("Order Accept"), anyString());
    }

    @Test
    void testProcessNotification_Rejected() {
        event.setStatus("REJECTED");

        when(menuItemRepository.findAllById(event.getMenuItemIds())).thenReturn(List.of(item1, item2));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(restaurant.getId())).thenReturn(Optional.of(restaurant));

        notificationService.processNotification(event);

        verify(emailService, times(1))
                .sendEmail(eq(user.getEmail()), contains("Order Rejected"), anyString());
    }

    @Test
    void testProcessNotification_PaymentSuccess() {
        event.setStatus("PAYMENT_SUCCESS");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(restaurant.getId())).thenReturn(Optional.of(restaurant));

        notificationService.processNotification(event);

        verify(emailService, times(1))
                .sendEmail(eq(user.getEmail()), contains("Payment Successful"), anyString());
    }

    @Test
    void testProcessNotification_UserNotFound() {
        event.setStatus("ACCEPTED");

        when(menuItemRepository.findAllById(event.getMenuItemIds())).thenReturn(List.of(item1, item2));
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        notificationService.processNotification(event);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testProcessNotification_RestaurantNotFound() {
        event.setStatus("PLACED");

        // Only stub restaurant to simulate not found
        when(restaurantRepository.findById(restaurant.getId())).thenReturn(Optional.empty());

        // Stub user to prevent NPE if called (lenient)
        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        lenient().when(menuItemRepository.findAllById(event.getMenuItemIds())).thenReturn(List.of(item1, item2));

        notificationService.processNotification(event);

        // No email should be sent
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testProcessNotification_UnknownStatus() {
        event.setStatus("INVALID_STATUS");

        notificationService.processNotification(event);

        verifyNoInteractions(emailService);
    }
}

