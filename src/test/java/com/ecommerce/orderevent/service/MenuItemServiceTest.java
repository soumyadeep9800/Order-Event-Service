package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.MenuItemRequestDto;
import com.ecommerce.orderevent.dtos.MenuItemResponseDto;
import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.MenuItemRepository;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @InjectMocks
    private MenuItemService menuItemService;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private RestaurantRepository restaurantRepository;

    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp(){
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");

        menuItem = new MenuItem();
        menuItem.setId(10L);
        menuItem.setName("Pizza");
        menuItem.setPrice(12.5);
        menuItem.setRestaurant(restaurant);
    }

    @Test
    void testAddMenuItem_Success(){
        MenuItemRequestDto requestDto = new MenuItemRequestDto();
        requestDto.setName("Pizza");
        requestDto.setPrice(12.5);
        requestDto.setRestaurantId(1L);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemResponseDto result = menuItemService.addMenuItem(1L, requestDto);

        assertNotNull(result);
        assertEquals("Pizza", result.getName());
        assertEquals("Test Restaurant", result.getRestaurant().getName());

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(menuItemRepository, times(1)).save(captor.capture());

        MenuItem captured = captor.getValue();
        assertEquals("Pizza", captured.getName());
        assertEquals(12.5, captured.getPrice());
        assertEquals(restaurant, captured.getRestaurant());
    }

    @Test
    void testAddMenuItem_RestaurantNotFound() {
        MenuItemRequestDto requestDto = new MenuItemRequestDto();
        requestDto.setName("Pizza");
        requestDto.setPrice(12.5);
        requestDto.setRestaurantId(1L);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.addMenuItem(1L, requestDto));
        verify(menuItemRepository, never()).save(any(MenuItem.class));
    }

    @Test
    void testGetMenuItemsByRestaurant_Success() {
        restaurant.setMenuItems(Collections.singletonList(menuItem));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        List<MenuItem> items = menuItemService.getMenuItemsByRestaurant(1L);
        assertEquals(1, items.size());
        assertEquals("Pizza", items.get(0).getName());
    }

    @Test
    void testGetMenuItemsByRestaurant_NotFound() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.getMenuItemsByRestaurant(1L));
    }

    @Test
    void testUpdateMenuItem_Success() {
        MenuItemRequestDto requestDto = new MenuItemRequestDto();
        requestDto.setName("Burger");
        requestDto.setDescription("Cheesy Burger");
        requestDto.setPrice(8.5);
        requestDto.setRestaurantId(1L);

        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemResponseDto result = menuItemService.updateMenuItem(10L, requestDto);

        assertNotNull(result);
        assertEquals("Burger", result.getName());
        assertEquals("Cheesy Burger", result.getDescription());
        assertEquals(8.5, result.getPrice());
        assertEquals("Test Restaurant", result.getRestaurant().getName());
        verify(menuItemRepository, times(1)).findById(10L);
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void testUpdateMenuItem_NotFound() {
        MenuItemRequestDto requestDto = new MenuItemRequestDto();
        requestDto.setName("Burger");

        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.updateMenuItem(99L, requestDto));
        verify(menuItemRepository, never()).save(any(MenuItem.class));
    }

    @Test
    void testFindMenuItem_Success() {
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
        MenuItem result = menuItemService.findMenuItem(10L);
        assertNotNull(result);
        assertEquals("Pizza", result.getName());
    }

    @Test
    void testFindMenuItem_NotFound() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.findMenuItem(99L));
    }

    @Test
    void testDeleteMenuItem_Success() {
        Long menuItemId = 10L;
        when(menuItemRepository.existsById(menuItemId)).thenReturn(true);
        doNothing().when(menuItemRepository).deleteById(menuItemId);
        menuItemService.deleteMenuItem(menuItemId);
        verify(menuItemRepository, times(1)).existsById(menuItemId);
        verify(menuItemRepository, times(1)).deleteById(menuItemId);
    }

    @Test
    void testDeleteMenuItem_NotFound() {
        Long menuItemId = 99L;
        when(menuItemRepository.existsById(menuItemId)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.deleteMenuItem(menuItemId));
        verify(menuItemRepository, times(1)).existsById(menuItemId);
        verify(menuItemRepository, never()).deleteById(menuItemId);
    }
}
