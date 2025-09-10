package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dto.ApiResponse;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/public")
@Tag(name = "Public APIs", description = "Endpoints accessible without authentication")
public class PublicController {

    private final UserService userService;
    public PublicController(UserService userService){
        this.userService=userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            ApiResponse<User> response = new ApiResponse<>("success", "User saved successfully!", savedUser, LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
        } catch (IllegalArgumentException ex) {
            ApiResponse<User> response = new ApiResponse<>("error", ex.getMessage(),  null, LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody Map<String,String> loginRequest){
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        try {
            User user = userService.getByEmail(email);
            ApiResponse<User> response = new ApiResponse<>("success", "User login successfully!", user, LocalDateTime.now());
            if (user.getPassword().equals(password)) {
                return ResponseEntity.ok(response);     // 200
            } else {
                response = new ApiResponse<>("failed", "User login failed!", user, LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);  // 401
            }
        } catch (IllegalArgumentException ex) {
            ApiResponse<User> response = new ApiResponse<>("failed", ex.getMessage(), null, LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404
        }
    }
}
