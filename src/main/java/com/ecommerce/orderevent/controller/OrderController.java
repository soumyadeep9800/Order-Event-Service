package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
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
        List<Long> menuItemIds = (List<Long>) request.get("menuitemIds");

        Order order = orderService.placeOrder(userId, restaurantId, menuItemIds);
        return ResponseEntity.ok(order);
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
