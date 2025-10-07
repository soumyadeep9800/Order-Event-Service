package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.User;
import com.ecommerce.orderevent.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("notfound@example.com")
        );

        verify(userRepository, times(1)).findByEmail("notfound@example.com");
    }
}

