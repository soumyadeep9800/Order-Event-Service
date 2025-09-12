package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dtos.ApiResponse;
import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;
import com.ecommerce.orderevent.dtos.UserRequestDto;
import com.ecommerce.orderevent.dtos.UserResponseDto;
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
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@RequestBody UserRequestDto requestDto) {
            User savedUser = userService.saveUser(requestDto); // throws ResourceNotFoundException if not found

            UserResponseDto responseDto = new UserResponseDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
            ApiResponse<UserResponseDto> response = new ApiResponse<>(
                    SUCCESS,
                    "User saved successfully!",
                    responseDto,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDto>> login(@RequestBody UserRequestDto requestDto){
        User user = userService.getByEmail(requestDto.getEmail()); // throws ResourceNotFoundException if not found
        if (!user.getPassword().equals(requestDto.getPassword())) throw new IllegalArgumentException("Invalid password!");

        UserResponseDto responseDto = new UserResponseDto(user.getId(), user.getName(), user.getEmail());
        ApiResponse<UserResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "User login successfully!",
                responseDto,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);     // 200
    }
}
