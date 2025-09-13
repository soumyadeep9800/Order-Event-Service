package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dtos.ApiResponse;
import com.ecommerce.orderevent.dtos.UserRequestDto;
import com.ecommerce.orderevent.dtos.UserResponseDto;
import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService){
        this.userService=userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> allUser = userService.getAllUser();
        ApiResponse<List<User>> response = new ApiResponse<>(
                SUCCESS,
                "Users fetched successfully!",
                allUser,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        User user = userService.getByEmail(email);
        ApiResponse<User> response = new ApiResponse<>(
                SUCCESS,
                "User fetched successfully!",
                user,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable Long id, @RequestBody UserRequestDto requestDto) {
        UserResponseDto user = userService.updateUser(id, requestDto);
        ApiResponse<UserResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "User update successfully!",
                user,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@PathVariable Long id) {
        List<Order> orders = userService.getUserOrders(id);
        ApiResponse<List<Order>> response = new ApiResponse<>(
                SUCCESS,
                "Order fetched successfully!",
                orders,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        ApiResponse<Void> response = new ApiResponse<>(
                SUCCESS,
                "User deleted successfully!",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
