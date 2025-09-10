package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "Endpoints for managing orders")
public class OrderController {
    private final OrderService orderService;
    public  OrderController(OrderService orderService){
        this.orderService=orderService;
    }

    @PostMapping()
    public ResponseEntity<Order> placeOrder(@RequestBody Map<String, Object> request){
        Long userId = Long.valueOf(request.get("UserId").toString());
        Long restaurantId = Long.valueOf(request.get("restaurantId").toString());

        @SuppressWarnings("unchecked")
        List<Long> menuItemIds = (List<Long>) request.get("menuItemIds");

        Order order = orderService.placeOrder(userId, restaurantId, menuItemIds);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable Long orderId){
        Order order = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Order>> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        List<Order> orders = orderService.getOrdersByRestaurant(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId){
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId){
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
