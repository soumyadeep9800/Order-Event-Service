package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dtos.ApiResponse;
import com.ecommerce.orderevent.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;

@RestController
@RequestMapping("/order")
@Tag(name = "Order Accept-Reject", description = "Endpoints for Accept/Reject orders")
public class OrderManagementController {
    private final OrderService orderService;
    public  OrderManagementController(OrderService orderService){
        this.orderService=orderService;
    }

    @GetMapping("/{orderId}/accept")
    public ResponseEntity<ApiResponse<String>> acceptOrder(@PathVariable Long orderId) {
        String status = orderService.updateOrderStatusForRestaurant(orderId, "ACCEPTED");
        ApiResponse<String> response = new ApiResponse<>(
                SUCCESS,
                "Order accepted successfully!",
                status,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectOrder(@PathVariable Long orderId) {
        String status = orderService.updateOrderStatusForRestaurant(orderId, "REJECTED");
        ApiResponse<String> response = new ApiResponse<>(
                SUCCESS,
                "Order rejected successfully!",
                status,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }
}
