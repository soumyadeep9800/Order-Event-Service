package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dtos.ApiResponse;
import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;
import com.ecommerce.orderevent.dtos.UserRequestDto;
import com.ecommerce.orderevent.dtos.UserResponseDto;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.service.UserDetailsServiceImpl;
import com.ecommerce.orderevent.service.UserService;
import com.ecommerce.orderevent.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/public")
@Tag(name = "Public APIs", description = "Endpoints accessible without authentication")
public class PublicController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    public PublicController(UserService userService, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService, AuthenticationManager authenticationManager) {
        this.userService=userService;
        this.jwtUtil=jwtUtil;
        this.userDetailsService=userDetailsService;
        this.authenticationManager=authenticationManager;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Void>> check(){
        ApiResponse<Void> response = new ApiResponse<>(
                SUCCESS,
                "Server is running!",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account and return JWT token")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@RequestBody UserRequestDto requestDto) {
        UserResponseDto savedUserDto = userService.saveUser(requestDto);
        User savedUserEntity = userService.getByEmail(savedUserDto.getEmail()); // fetch entity
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUserEntity.getEmail());
        String token = jwtUtil.generateToken(userDetails.getUsername());

        // Build response DTO with token
        UserResponseDto savedUser = UserResponseDto.fromEntity(savedUserEntity).withToken(token);

        ApiResponse<UserResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "User saved successfully!",
                savedUser,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate a user with email and password and return JWT token")
    public ResponseEntity<ApiResponse<UserResponseDto>> login(@RequestBody UserRequestDto requestDto) {
        try {
            // 1️⃣ Authenticate credentials using Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(),   // username (email)
                            requestDto.getPassword() // raw password
                    )
            );

            // 2️⃣ Load the user details from the DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(requestDto.getEmail());

            // 3️⃣ Generate JWT token using the username
            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            // 4️⃣ Build response DTO
            User user = userService.getByEmail(requestDto.getEmail());
            UserResponseDto responseDto = UserResponseDto.fromEntity(user).withToken(jwt);

            ApiResponse<UserResponseDto> response = new ApiResponse<>(
                    SUCCESS,
                    "User logged in successfully!",
                    responseDto,
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            ApiResponse<UserResponseDto> errorResponse = new ApiResponse<>(
                    "FAILURE",
                    "Incorrect username or password",
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}

//    @PostMapping("/register")
//    @Operation(summary = "Register new user", description = "Create a new user account and save it to the system")
//    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@RequestBody UserRequestDto requestDto) {
//        UserResponseDto savedUser = userService.saveUser(requestDto); // throws ResourceNotFoundException if not found
//            ApiResponse<UserResponseDto> response = new ApiResponse<>(
//                    SUCCESS,
//                    "User saved successfully!",
//                    savedUser,
//                    LocalDateTime.now()
//            );
//            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
//    }

//    @PostMapping("/login")
//    @Operation(summary = "User login", description = "Authenticate a user with email and password")
//    public ResponseEntity<ApiResponse<UserResponseDto>> login(@RequestBody UserRequestDto requestDto) {
//        User user = userService.getByEmail(requestDto.getEmail());
//        if (!user.getPassword().equals(requestDto.getPassword())) throw new IllegalArgumentException("Invalid password!");
//
//        UserResponseDto responseDto = UserResponseDto.fromEntity(user);
//        ApiResponse<UserResponseDto> response = new ApiResponse<>(
//                SUCCESS,
//                "User login successfully!",
//                responseDto,
//                LocalDateTime.now()
//        );
//        return ResponseEntity.ok(response); //200
//    }
