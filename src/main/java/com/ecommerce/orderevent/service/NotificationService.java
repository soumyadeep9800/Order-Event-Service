package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.models.OrderEvent;
import com.ecommerce.orderevent.repository.UserRepository;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public NotificationService(EmailService emailService,
                               UserRepository userRepository,
                               RestaurantRepository restaurantRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public void processNotification(OrderEvent event) {
        try {
            switch (event.getStatus()) {
                case "PLACED" -> {
                    // Notify restaurant (lookup from DB)
                    Restaurant restaurant = restaurantRepository.findById(event.getRestaurantId())
                            .orElseThrow(() -> new RuntimeException("Restaurant not found: " + event.getRestaurantId()));

                    emailService.sendEmail(
                            restaurant.getEmail(),
                            "🍽 New Order Received",
                            "Order #" + event.getOrderId() + " placed by User " + event.getUserId()
                                    + "\nItems: " + event.getMenuItemIds()
                    );
                    log.info("📧 Sent 'PLACED' email to restaurant {}", restaurant.getEmail());
                }
                case "ACCEPTED" -> {
                    // Notify user (lookup from DB)
                    User user = userRepository.findById(event.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found: " + event.getUserId()));

                    emailService.sendEmail(
                            user.getEmail(),
                            "✅ Order Confirmed",
                            "Your order #" + event.getOrderId() + " was accepted!"
                    );
                    log.info("📧 Sent 'ACCEPTED' email to user {}", user.getEmail());
                }
                case "REJECTED" -> {
                    User user = userRepository.findById(event.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found: " + event.getUserId()));

                    emailService.sendEmail(
                            user.getEmail(),
                            "❌ Order Rejected",
                            "Sorry, your order #" + event.getOrderId() + " was rejected."
                    );
                    log.info("📧 Sent 'REJECTED' email to user {}", user.getEmail());
                }
                default -> log.warn("⚠️ Unknown order status: {}", event.getStatus());
            }
        } catch (Exception e) {
            log.error("❌ Failed to process notification for event {} due to {}", event, e.getMessage(), e);
            throw e; // ❗ rethrow so Kafka can retry delivery
        }
    }
}

