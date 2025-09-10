package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.*;

import static com.ecommerce.orderevent.constants.ErrorMessages.USER_NOT_FOUND;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public User saveUser(User user){
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists with this email!");
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException( USER_NOT_FOUND + id));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(updatedUser.getPassword());

        return userRepository.save(existingUser);
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
