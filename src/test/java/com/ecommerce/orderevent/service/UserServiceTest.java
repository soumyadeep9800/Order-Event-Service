package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private  UserService userService;
    @Mock
    private  UserRepository userRepository;

    @Test
    void testSaveUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        userService.saveUser(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testSaveUser_AlreadyExists(){
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
            userService.saveUser(user);
        });
        assertEquals("User already exists with this email!", exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    void testGetAllUser(){
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1,user2));
        List<User> result = userService.getAllUser();
        assertEquals(2,result.size());
        assertEquals("user1@example.com",result.get(0).getEmail());
    }

    @Test
    void testGetByEmail_Found(){
        User user = new User();
        user.setId(1L);
        user.setEmail("found@example.com");

        when(userRepository.findByEmail("found@example.com")).thenReturn(Optional.of(user));
        Optional<User> result = userService.getByEmail("found@example.com");
        assertTrue(result.isPresent());
        assertEquals("found@example.com", result.get().getEmail());
    }

    @Test
    void testGetByEmail_NotFound(){
        when(userRepository.findByEmail("Notfound@example.com")).thenReturn(Optional.empty());
        Optional<User> result = userService.getByEmail("Notfound@example.com");
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteById(){
        Long userId = 1L;
        userService.deleteById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }
}
