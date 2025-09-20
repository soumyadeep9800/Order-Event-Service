package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.models.OrderEvent;
import com.ecommerce.orderevent.repository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.ecommerce.orderevent.constants.ErrorMessages.ORDER_ITEM_NOT_FOUND;

@Service
public class PaymentService {

    private static final String ORDER_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public PaymentService(OrderRepository orderRepository,
                          KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String initiatePayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_ITEM_NOT_FOUND + orderId));

        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new IllegalStateException("Payment can only be initiated for accepted orders.");
        }

        OrderEvent event = new OrderEvent(
                order.getId(),
                order.getUser().getId(),
                order.getRestaurant().getId(),
                order.getItems().stream().map(MenuItem::getId).toList(),
                "PAYMENT_SUCCESS"
        );

        kafkaTemplate.send(ORDER_TOPIC, event);
        return "Payment started for order";
    }
}

