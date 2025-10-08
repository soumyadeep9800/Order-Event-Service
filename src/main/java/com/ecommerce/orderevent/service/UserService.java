package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.UserRequestDto;
import com.ecommerce.orderevent.dtos.UserResponseDto;
import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

import static com.ecommerce.orderevent.constants.ErrorMessages.USER_NOT_FOUND;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public UserResponseDto saveUser(UserRequestDto requestDto){
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists with this email!");
        }

        User user = new User();
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        User saveUser = userRepository.save(user);

        return UserResponseDto.fromEntity(saveUser);
    }

    public UserResponseDto updateUser(Long id, UserRequestDto updatedUserDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException( USER_NOT_FOUND + id));

        existingUser.setName(updatedUserDto.getName());
        existingUser.setEmail(updatedUserDto.getEmail());
        existingUser.setPassword(updatedUserDto.getPassword());

        User savedUser = userRepository.save(existingUser);
        return UserResponseDto.fromEntity(savedUser);
    }

    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + email));
    }


    public List<Order> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + userId));
        return user.getOrders();
    }

    public void deleteById(Long id){
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(USER_NOT_FOUND + id);
        }
        userRepository.deleteById(id);
    }
}
