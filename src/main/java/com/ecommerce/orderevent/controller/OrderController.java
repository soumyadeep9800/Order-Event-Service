package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dtos.OrderRequestDto;
import com.ecommerce.orderevent.dtos.OrderResponseDto;
import com.ecommerce.orderevent.dtos.OrderStatusUpdateRequestDto;
import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Place new order", description = "Create a new order and save it to the system")
    public ResponseEntity<ApiResponse<OrderResponseDto>> placeOrder(@RequestBody @Valid OrderRequestDto requestDto) {
        OrderResponseDto responseDto = orderService.placeOrder(requestDto);
        ApiResponse<OrderResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "Order placed successfully!",
                responseDto,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Fetch order details", description = "Retrieve detailed information of a specific order by its ID")
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
    @Operation(summary = "Fetch orders by restaurant", description = "Retrieve all orders placed for a specific restaurant by its ID")
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

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> getOrderStatus(@PathVariable Long id) {
        String status = orderService.getOrderStatus(id);
        ApiResponse<String> response = new ApiResponse<>(
                SUCCESS,
                "Order Status fetched successfully",
                status,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel order", description = "Cancel an existing order by its ID")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId){
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "Update order status", description = "Update the status of an existing order by its ID")
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

}
