package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.models.OrderEvent;
import com.ecommerce.orderevent.repository.MenuItemRepository;
import com.ecommerce.orderevent.repository.UserRepository;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import static com.ecommerce.orderevent.constants.ErrorMessages.*;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public NotificationService(EmailService emailService,
                               UserRepository userRepository,
                               RestaurantRepository restaurantRepository,
                               MenuItemRepository menuItemRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository=menuItemRepository;
    }

    private String buildItemDetails(List<MenuItem> items) {
        StringBuilder itemDetails = new StringBuilder();
        for (MenuItem item : items) {
            itemDetails.append("- ")
                    .append(item.getName())
                    .append(" (₹").append(item.getPrice()).append(")")
                    .append("\n");
        }
        return itemDetails.toString();
    }

    public void processNotification(OrderEvent event) {
        try {
            switch (event.getStatus()) {
                case "PLACED" -> {
                    // Fetch items
                    List<MenuItem> items = menuItemRepository.findAllById(event.getMenuItemIds());
                    double totalPrice = items.stream()
                            .mapToDouble(MenuItem::getPrice)
                            .sum();
                    String itemDetails = buildItemDetails(items);

                    // Notify restaurant
                    Restaurant restaurant = restaurantRepository.findById(event.getRestaurantId())
                            .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ event.getRestaurantId()));
                    String acceptLink = "http://localhost:8080/orders/" + event.getOrderId() + "/accept";
                    String rejectLink = "http://localhost:8080/orders/" + event.getOrderId() + "/reject";
                    emailService.sendEmail(
                            restaurant.getEmail(),
                            "🍽 New Order Received - #" + event.getOrderId(),
                            "Hello " + restaurant.getName() + ",\n\n" +
                                    "You have received a new order!\n\n" +
                                    "Order ID: " + event.getOrderId() + "\n" +
                                    "Customer ID: " + event.getUserId() + "\n\n" +
                                    "Ordered Items:\n" + itemDetails + "\n" +
                                    "Total Price: ₹" + totalPrice + "\n\n" +
                                    "Accept Order: " + acceptLink + "\n" +      // ✅ accept link
                                    "Reject Order: " + rejectLink + "\n\n" +    // ✅ reject link
                                    "Please take action at your earliest convenience.\n\n" +
                                    "— Order Event Service 🍴"
                    );
                    log.info("📧 Sent 'PLACED' email to restaurant {}", restaurant.getEmail());

                    // Notify user
                    log.info("🔎 Fetching user {} for order {}", event.getUserId(), event.getOrderId());
                    User user = userRepository.findById(event.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND+ event.getUserId()));

                    log.info("✅ Found user: {} with email {}", user.getName(), user.getEmail());

                    emailService.sendEmail(
                            user.getEmail(),
                            "🛒 Order PLACED - #" + event.getOrderId(),
                            "Hi " + user.getName() + ",\n\n" +
                                    "Thank you for your order! Here are the details:\n\n" +
                                    "Order ID: " + event.getOrderId() + "\n" +
                                    "Restaurant: " + restaurant.getName() + "\n\n" +
                                    "Items:\n" + itemDetails +
                                    "\nTotal Price: ₹" + totalPrice + "\n\n" +
                                    "We’ll notify you once the restaurant accepts your order.\n\n" +
                                    "— Order Event Service 🍴"
                    );
                    log.info("📧 Sent 'PLACED' confirmation email to user {}", user.getEmail());
                }
                case "ACCEPTED" -> {
                    String paymentLink = "http://localhost:8080/payments/" + event.getOrderId() + "/pay";
                    // Fetch items for nicer email
                    List<MenuItem> items = menuItemRepository.findAllById(event.getMenuItemIds());
                    double totalPrice = items.stream()
                            .mapToDouble(MenuItem::getPrice)
                            .sum();
                    String itemDetails = buildItemDetails(items);

                    User user = userRepository.findById(event.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + event.getUserId()));

                    Restaurant restaurant = restaurantRepository.findById(event.getRestaurantId())
                            .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND + event.getRestaurantId()));

                    emailService.sendEmail(
                            user.getEmail(),
                            "✅ Order Accept - #" + event.getOrderId(),
                            "Hi " + user.getName() + ",\n\n" +
                                    "Good news! Your order has been accepted by **" + restaurant.getName() + "** 🎉\n\n" +
                                    "Order Details:\n" +
                                    "Order ID: " + event.getOrderId() + "\n" +
                                    "Items:\n" + itemDetails +
                                    "\nTotal Price: ₹" + totalPrice + "\n\n" +
                                    "Please complete your payment by clicking the link below:\n" +
                                    paymentLink + "\n\n" +
                                    "Your food will be prepared once payment is confirmed.\n\n" +
                                    "— Order Event Service 🍴"
                    );
                    log.info("📧 Sent 'ACCEPTED' email to user {}", user.getEmail());
                }
                case "REJECTED" -> {
                    List<MenuItem> items = menuItemRepository.findAllById(event.getMenuItemIds());
                    String itemDetails = buildItemDetails(items);

                    User user = userRepository.findById(event.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + event.getUserId()));

                    Restaurant restaurant = restaurantRepository.findById(event.getRestaurantId())
                            .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND + event.getRestaurantId()));

                    emailService.sendEmail(
                            user.getEmail(),
                            "❌ Order Rejected - #" + event.getOrderId(),
                            "Hi " + user.getName() + ",\n\n" +
                                    "We’re sorry to inform you that your order has been rejected by **" + restaurant.getName() + "** 😞\n\n" +
                                    "Order Details:\n" +
                                    "Order ID: " + event.getOrderId() + "\n" +
                                    "Items:\n" + itemDetails + "\n\n" +
                                    "No worries — you can try ordering from another restaurant.\n\n" +
                                    "— Order Event Service 🍴"
                    );
                    log.info("📧 Sent 'REJECTED' email to user {}", user.getEmail());
                }
                case "PAYMENT_SUCCESS" -> {
                    User user = userRepository.findById(event.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + event.getUserId()));

                    Restaurant restaurant = restaurantRepository.findById(event.getRestaurantId())
                            .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND + event.getRestaurantId()));

                    emailService.sendEmail(
                            user.getEmail(),
                            "💳 Payment Successful - #" + event.getOrderId(),
                            "Hi " + user.getName() + ",\n\n" +
                                    "Your payment for order #" + event.getOrderId() + " has been successfully processed. 🎉\n\n" +
                                    "Restaurant: " + restaurant.getName() + "\n\n" +
                                    "The restaurant will now start preparing your food.\n\n" +
                                    "Thank you for choosing us!\n\n" +
                                    "— Order Event Service 🍴"
                    );
                    log.info("📧 Sent 'PAYMENT_SUCCESS' email to user {}", user.getEmail());
                }
                default -> log.warn("⚠️ Unknown order status: {}", event.getStatus());
            }
        } catch (Exception e) {
            log.error("❌ Failed to process notification for event {} due to {}", event, e.getMessage(), e);
        }
    }
}

