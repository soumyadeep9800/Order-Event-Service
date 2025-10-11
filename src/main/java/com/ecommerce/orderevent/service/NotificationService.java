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
        this.menuItemRepository = menuItemRepository;
    }

    private String buildItemDetails(List<MenuItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table style=\"width:100%;border-collapse:collapse;\">")
                .append("<thead><tr><th style=\"text-align:left;padding:8px;border-bottom:1px solid #ddd;\">Item</th>")
                .append("<th style=\"text-align:right;padding:8px;border-bottom:1px solid #ddd;\">Price</th></tr></thead><tbody>");
        for (MenuItem item : items) {
            sb.append("<tr>")
                    .append("<td style=\"padding:8px;border-bottom:1px solid #eee;\">").append(item.getName()).append("</td>")
                    .append("<td style=\"padding:8px;border-bottom:1px solid #eee;text-align:right;\">₹").append(item.getPrice()).append("</td>")
                    .append("</tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }

    public void processNotification(OrderEvent event) {
        try {
            List<MenuItem> items = menuItemRepository.findAllById(event.getMenuItemIds());
            String itemDetails = buildItemDetails(items);
            double totalPrice = items.stream().mapToDouble(MenuItem::getPrice).sum();

            User user = userRepository.findById(event.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + event.getUserId()));

            Restaurant restaurant = restaurantRepository.findById(event.getRestaurantId())
                    .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND + event.getRestaurantId()));

            switch (event.getStatus()) {
                case "PLACED" -> {
                    String acceptLink = "http://localhost:8080/order/" + event.getOrderId() + "/accept";
                    String rejectLink = "http://localhost:8080/order/" + event.getOrderId() + "/reject";

                    String restaurantHtml = """
                            <html><body style="font-family:Arial,sans-serif;color:#333;">
                            <div style="max-width:600px;margin:auto;border:1px solid #ddd;border-radius:8px;padding:20px;">
                                <h2 style="color:#28a745;">🍽 New Order Received - #%s</h2>
                                <p>Hello <strong>%s</strong>,</p>
                                <p>You have a new order! Please review the details below:</p>
                                <p><strong>Order ID:</strong> %s<br>
                                   <strong>Customer ID:</strong> %s</p>
                                <h3 style="color:#555;">Ordered Items:</h3>
                                %s
                                <p style="text-align:right;font-size:16px;"><strong>Total Price: ₹%s</strong></p>
                                <div style="margin-top:20px;text-align:center;">
                                    <a href="%s" style="background:#28a745;color:#fff;padding:10px 15px;text-decoration:none;border-radius:6px;font-weight:bold;">✅ Accept Order</a>
                                    &nbsp;
                                    <a href="%s" style="background:#dc3545;color:#fff;padding:10px 15px;text-decoration:none;border-radius:6px;font-weight:bold;">❌ Reject Order</a>
                                </div>
                                <p style="margin-top:20px;">Please take action as soon as possible.</p>
                                <hr>
                                <p style="text-align:center;color:#888;font-size:12px;">— Order Event Service 🍴</p>
                            </div>
                            </body></html>
                            """.formatted(event.getOrderId(), restaurant.getName(), event.getOrderId(),
                            event.getUserId(), itemDetails, totalPrice, acceptLink, rejectLink);

                    emailService.sendEmail(restaurant.getEmail(),
                            "🍽 New Order Received - #" + event.getOrderId(),
                            restaurantHtml);

                    log.info("📧 Sent 'PLACED' email to restaurant {}", restaurant.getEmail());

                    String userHtml = """
                            <html><body style="font-family:Arial,sans-serif;color:#333;">
                            <div style="max-width:600px;margin:auto;border:1px solid #ddd;border-radius:8px;padding:20px;">
                                <h2 style="color:#007bff;">🛒 Order Placed - #%s</h2>
                                <p>Hi <strong>%s</strong>,</p>
                                <p>Thank you for your order! Here are your details:</p>
                                <p><strong>Order ID:</strong> %s<br>
                                   <strong>Restaurant:</strong> %s</p>
                                <h3 style="color:#555;">Items:</h3>
                                %s
                                <p style="text-align:right;font-size:16px;"><strong>Total Price: ₹%s</strong></p>
                                <p>We’ll notify you once the restaurant accepts your order.</p>
                                <hr>
                                <p style="text-align:center;color:#888;font-size:12px;">— Order Event Service 🍴</p>
                            </div>
                            </body></html>
                            """.formatted(event.getOrderId(), user.getName(), event.getOrderId(),
                            restaurant.getName(), itemDetails, totalPrice);

                    emailService.sendEmail(user.getEmail(),
                            "🛒 Order PLACED - #" + event.getOrderId(),
                            userHtml);

                    log.info("📧 Sent 'PLACED' confirmation email to user {}", user.getEmail());
                }
                case "ACCEPTED" -> {
                    String paymentLink = "http://localhost:8080/payments/" + event.getOrderId() + "/pay";

                    String html = """
                            <html><body style="font-family:Arial,sans-serif;color:#333;">
                            <div style="max-width:600px;margin:auto;border:1px solid #ddd;border-radius:8px;padding:20px;">
                                <h2 style="color:#28a745;">✅ Order Accepted - #%s</h2>
                                <p>Hi <strong>%s</strong>,</p>
                                <p>Your order has been accepted by <strong>%s</strong> 🎉</p>
                                <p><strong>Order ID:</strong> %s</p>
                                <h3 style="color:#555;">Items:</h3>
                                %s
                                <p style="text-align:right;font-size:16px;"><strong>Total Price: ₹%s</strong></p>
                                <p>Please complete your payment:</p>
                                <p style="text-align:center;">
                                    <a href="%s" style="background:#007bff;color:#fff;padding:10px 15px;text-decoration:none;border-radius:6px;font-weight:bold;">💳 Pay Now</a>
                                </p>
                                <p>Your food will be prepared once payment is confirmed.</p>
                                <hr>
                                <p style="text-align:center;color:#888;font-size:12px;">— Order Event Service 🍴</p>
                            </div>
                            </body></html>
                            """.formatted(event.getOrderId(), user.getName(), restaurant.getName(),
                            event.getOrderId(), itemDetails, totalPrice, paymentLink);

                    emailService.sendEmail(user.getEmail(),
                            "✅ Order Accepted - #" + event.getOrderId(),
                            html);

                    log.info("📧 Sent 'ACCEPTED' email to user {}", user.getEmail());
                }
                case "REJECTED" -> {
                    String html = """
                            <html><body style="font-family:Arial,sans-serif;color:#333;">
                            <div style="max-width:600px;margin:auto;border:1px solid #ddd;border-radius:8px;padding:20px;">
                                <h2 style="color:#dc3545;">❌ Order Rejected - #%s</h2>
                                <p>Hi <strong>%s</strong>,</p>
                                <p>Unfortunately, your order has been rejected by <strong>%s</strong> 😞</p>
                                <p><strong>Order ID:</strong> %s</p>
                                <h3 style="color:#555;">Items:</h3>
                                %s
                                <p>No worries — you can try ordering from another restaurant.</p>
                                <hr>
                                <p style="text-align:center;color:#888;font-size:12px;">— Order Event Service 🍴</p>
                            </div>
                            </body></html>
                            """.formatted(event.getOrderId(), user.getName(), restaurant.getName(),
                            event.getOrderId(), itemDetails);

                    emailService.sendEmail(user.getEmail(),
                            "❌ Order Rejected - #" + event.getOrderId(),
                            html);

                    log.info("📧 Sent 'REJECTED' email to user {}", user.getEmail());
                }
                case "PAYMENT_SUCCESS" -> {
                    String html = """
                            <html><body style="font-family:Arial,sans-serif;color:#333;">
                            <div style="max-width:600px;margin:auto;border:1px solid #ddd;border-radius:8px;padding:20px;">
                                <h2 style="color:#28a745;">💳 Payment Successful - #%s</h2>
                                <p>Hi <strong>%s</strong>,</p>
                                <p>Your payment for order <strong>#%s</strong> has been successfully processed 🎉</p>
                                <p><strong>Restaurant:</strong> %s</p>
                                <p>The restaurant will now start preparing your food.</p>
                                <hr>
                                <p style="text-align:center;color:#888;font-size:12px;">— Order Event Service 🍴</p>
                            </div>
                            </body></html>
                            """.formatted(event.getOrderId(), user.getName(), event.getOrderId(), restaurant.getName());

                    emailService.sendEmail(user.getEmail(),
                            "💳 Payment Successful - #" + event.getOrderId(),
                            html);

                    log.info("📧 Sent 'PAYMENT_SUCCESS' email to user {}", user.getEmail());
                }
                default -> log.warn("⚠️ Unknown order status: {}", event.getStatus());
            }
        } catch (Exception e) {
            log.error("❌ Failed to process notification for event {} due to {}", event, e.getMessage(), e);
        }
    }
}

