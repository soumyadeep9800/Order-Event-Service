package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.UserRequestDto;
import com.ecommerce.orderevent.dtos.UserResponseDto;
import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.ecommerce.orderevent.constants.ErrorMessages.*;
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
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("Test User");
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password123");
        // Simulate DB: no existing user
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        // Mock repository save() to return a user with ID
        User savedUserMock = new User();
        savedUserMock.setId(1L);
        savedUserMock.setName("Test User");
        savedUserMock.setEmail("test@example.com");
        savedUserMock.setPassword("password123");

        when(userRepository.save(any(User.class))).thenReturn(savedUserMock);
        UserResponseDto result = userService.saveUser(requestDto);
        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(1L, result.getId());
    }


    @Test
    void testSaveUser_AlreadyExists(){
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("Test User");
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password123");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Existing User");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
            userService.saveUser(requestDto);
        });
        assertEquals("User already exists with this email!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_Success() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("Test User");
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password123");
        // Existing user in DB
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword("oldPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        UserResponseDto result = userService.updateUser(1L, requestDto);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testUpdateUser_NotFound() {
        UserRequestDto updatedUser = new UserRequestDto();
        updatedUser.setName("New Name");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("newPass");

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(2L, updatedUser);
        });

        assertEquals(USER_NOT_FOUND + 2L, exception.getMessage());
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, never()).save(any(User.class));
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
        User result = userService.getByEmail("found@example.com");
        assertNotNull(result);
        assertEquals("found@example.com", result.getEmail());
    }

    @Test
    void testGetByEmail_NotFound(){
        when(userRepository.findByEmail("Notfound@example.com"))
                .thenReturn(Optional.empty()); // simulate user not found

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getByEmail("Notfound@example.com");
        });
        assertEquals( USER_NOT_FOUND + "Notfound@example.com", exception.getMessage());
    }

    @Test
    void testGetUserOrders_Success() {
        User user = new User();
        user.setId(1L);

        Order order1 = new Order();
        order1.setId(101L);
        Order order2 = new Order();
        order2.setId(102L);
        user.setOrders(List.of(order1, order2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        List<Order> result = userService.getUserOrders(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).getId());
        assertEquals(102L, result.get(1).getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserOrders_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserOrders(99L));
        assertEquals(USER_NOT_FOUND + 99L, ex.getMessage());
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void testDeleteById(){
        User user = new User();
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}
