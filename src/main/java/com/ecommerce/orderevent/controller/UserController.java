package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user){
        User saveUser = userService.saveUser(user);
        return ResponseEntity.ok(saveUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String,String> loginRequest){
        String email= loginRequest.get("email");
        String password= loginRequest.get("password");

        return userService.getByEmail(email)
                .map(user -> {
                    if(user.getPassword().equals(password)){
                        return ResponseEntity.ok("Login Successful!");
                    }else{
                        return ResponseEntity.badRequest().body("Invalid password");
                    }
                })
                .orElse(ResponseEntity.badRequest().body("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userService.getByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok("User deleted ✅");
    }
}
