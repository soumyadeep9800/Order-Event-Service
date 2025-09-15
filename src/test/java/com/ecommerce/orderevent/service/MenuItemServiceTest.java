package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.MenuItemRepository;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.save(menuItem)).thenReturn(menuItem);

        MenuItem result = menuItemService.addMenuItem(1L, menuItem);

        assertNotNull(result);
        assertEquals("Pizza", result.getName());
        assertEquals(restaurant, result.getRestaurant());
        verify(menuItemRepository, times(1)).save(menuItem);
    }

    @Test
    void testAddMenuItem_RestaurantNotFound() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.addMenuItem(1L, menuItem));
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
        MenuItem updatedMenuItem = new MenuItem();
        updatedMenuItem.setName("Burger");
        updatedMenuItem.setDescription("Cheesy Burger");
        updatedMenuItem.setPrice(8.5);
        updatedMenuItem.setRestaurant(restaurant);

        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
        when(menuItemRepository.save(menuItem)).thenReturn(menuItem);
        MenuItem result = menuItemService.updateMenuItem(10L, updatedMenuItem);

        assertNotNull(result);
        assertEquals("Burger", result.getName());
        assertEquals("Cheesy Burger", result.getDescription());
        assertEquals(8.5, result.getPrice());
        assertEquals(restaurant, result.getRestaurant());
        verify(menuItemRepository, times(1)).findById(10L);
        verify(menuItemRepository, times(1)).save(menuItem);
    }

    @Test
    void testUpdateMenuItem_NotFound() {
        MenuItem updatedMenuItem = new MenuItem();
        updatedMenuItem.setName("Burger");
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.updateMenuItem(99L, updatedMenuItem));
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
