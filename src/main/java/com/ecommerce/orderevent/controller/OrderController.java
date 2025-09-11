package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ecommerce.orderevent.dtos.ApiResponse;
import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "Endpoints for managing orders")
public class OrderController {
    private final OrderService orderService;
    public  OrderController(OrderService orderService){
        this.orderService=orderService;
    }

    @PostMapping("/placeOrder")
    public ResponseEntity<ApiResponse<Order>> placeOrder(@RequestBody Map<String, Object> request){
        Long userId = Long.valueOf(request.get("UserId").toString());
        Long restaurantId = Long.valueOf(request.get("restaurantId").toString());
        @SuppressWarnings("unchecked")
        List<Long> menuItemIds = (List<Long>) request.get("menuItemIds");

        Order order = orderService.placeOrder(userId, restaurantId, menuItemIds);
        ApiResponse<Order> response = new ApiResponse<>(
                SUCCESS,
                "Order placed successfully!",
                order,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrderDetails(@PathVariable Long orderId){
        Order order = orderService.getOrderDetails(orderId);
        ApiResponse<Order> response = new ApiResponse<>(
                SUCCESS,
                "Order details fetched successfully!",
                order,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        List<Order> orders = orderService.getOrdersByRestaurant(restaurantId);
        ApiResponse<List<Order>> response = new ApiResponse<>(
                SUCCESS,
                "Order of the restaurant...",
                orders,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        ApiResponse<Order> response = new ApiResponse<>(
                SUCCESS,
                "Order Updated successfully!",
                updatedOrder,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId){
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
