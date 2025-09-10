package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user){
        Map<String, Object> response = new HashMap<>();
        try {
            userService.saveUser(user);
            response.put("status", "success");
            response.put("message", "User saved successfully!");
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
        } catch (IllegalArgumentException ex) {
            response.put("status", "error");
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String,String> loginRequest){
        String email= loginRequest.get("email");
        String password= loginRequest.get("password");

        User user = userService.getByEmail(email);
        if (user.getPassword().equals(password)) {
            return ResponseEntity.ok("Login Successful!");
        } else {
            return ResponseEntity.badRequest().body("Invalid password");
        }
    }
}
