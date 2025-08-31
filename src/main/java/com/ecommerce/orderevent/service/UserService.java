package com.ecommerce.orderevent.service;


import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public void saveUser(User user){
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists with this email!");
        }
        userRepository.save(user);
    }

    public List<User> getAll(){
        return userRepository.findAll();
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteById(Long id){
        userRepository.deleteById(id);
    }
}
