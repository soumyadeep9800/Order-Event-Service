package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dto.ApiResponse;
import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;
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
            User savedUser = userService.saveUser(user); // throws ResourceNotFoundException if not found
            ApiResponse<User> response = new ApiResponse<>(
                    SUCCESS,
                    "User saved successfully!",
                    savedUser,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody Map<String,String> loginRequest){
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        User user = userService.getByEmail(email); // throws ResourceNotFoundException if not found
        if (!user.getPassword().equals(password)) throw new IllegalArgumentException("Invalid password!");

        ApiResponse<User> response = new ApiResponse<>(
                SUCCESS,
                "User login successfully!",
                user,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);     // 200
    }
}
