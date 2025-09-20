package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.models.OrderEvent;
import com.ecommerce.orderevent.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private Order acceptedOrder;
    private Order pendingOrder;
    private User user;
    private Restaurant restaurant;
    private MenuItem item1;
    private MenuItem item2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        restaurant = new Restaurant();
        restaurant.setId(10L);

        item1 = new MenuItem();
        item1.setId(100L);

        item2 = new MenuItem();
        item2.setId(101L);

        // Accepted order
        acceptedOrder = new Order();
        acceptedOrder.setId(999L);
        acceptedOrder.setStatus("ACCEPTED");
        acceptedOrder.setUser(user);
        acceptedOrder.setRestaurant(restaurant);
        acceptedOrder.setItems(List.of(item1, item2));

        // Pending order
        pendingOrder = new Order();
        pendingOrder.setId(1000L);
        pendingOrder.setStatus("PLACED");
        pendingOrder.setUser(user);
        pendingOrder.setRestaurant(restaurant);
        pendingOrder.setItems(List.of(item1, item2));
    }

    @Test
    void testInitiatePayment_Success() {
        when(orderRepository.findById(acceptedOrder.getId())).thenReturn(Optional.of(acceptedOrder));

        String result = paymentService.initiatePayment(acceptedOrder.getId());

        assertEquals("Payment started for order", result);

        // Verify that a PAYMENT_SUCCESS event was sent to Kafka
        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(kafkaTemplate, times(1)).send(eq("order-events"), captor.capture());

        OrderEvent sentEvent = captor.getValue();
        assertEquals("PAYMENT_SUCCESS", sentEvent.getStatus());
        assertEquals(acceptedOrder.getId(), sentEvent.getOrderId());
        assertEquals(user.getId(), sentEvent.getUserId());
        assertEquals(restaurant.getId(), sentEvent.getRestaurantId());
        assertEquals(List.of(item1.getId(), item2.getId()), sentEvent.getMenuItemIds());
    }

    @Test
    void testInitiatePayment_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                paymentService.initiatePayment(999L));

        // Correct assertion
        assertTrue(ex.getMessage().contains("Order item not found with id: "+999));
        verifyNoInteractions(kafkaTemplate);
    }



    @Test
    void testInitiatePayment_OrderNotAccepted() {
        when(orderRepository.findById(pendingOrder.getId())).thenReturn(Optional.of(pendingOrder));

        Runnable action = () -> paymentService.initiatePayment(pendingOrder.getId());
        IllegalStateException ex = assertThrows(IllegalStateException.class, action::run);

        assertEquals("Payment can only be initiated for accepted orders.", ex.getMessage());
        verifyNoInteractions(kafkaTemplate);
    }

}
