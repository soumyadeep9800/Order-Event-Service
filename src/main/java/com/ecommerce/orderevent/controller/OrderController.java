package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dtos.OrderRequestDto;
import com.ecommerce.orderevent.dtos.OrderResponseDto;
import com.ecommerce.orderevent.dtos.OrderStatusUpdateRequestDto;
import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<OrderResponseDto>> placeOrder(@RequestBody @Valid OrderRequestDto requestDto){
        OrderResponseDto responseDto = orderService.placeOrder(requestDto);
        ApiResponse<OrderResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "Order placed successfully!",
                responseDto,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequestDto requestDto) {

        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, requestDto.getStatus());
        ApiResponse<OrderResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "Order updated successfully!",
                updatedOrder,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
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

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId){
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
