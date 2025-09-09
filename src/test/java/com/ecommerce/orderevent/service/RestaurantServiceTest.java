package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.ecommerce.orderevent.constants.ErrorMessages.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;
    @Mock
    private RestaurantRepository restaurantRepository;

    @Test
    void TestAddRestaurant_Success(){
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Pizza Hut");

        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setName("Chicken Pizza");

        restaurant.setMenuItems(Arrays.asList(item));

        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);
        Restaurant saved = restaurantService.addRestaurant(restaurant);
        assertNotNull(saved);
        assertEquals("Pizza Hut", saved.getName());
        assertEquals(restaurant, saved.getMenuItems().get(0).getRestaurant());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void testGetAllRestaurant(){
        Restaurant r1 = new Restaurant();
        r1.setId(1L);
        r1.setName("Pizza Hut");

        Restaurant r2 = new Restaurant();
        r2.setId(1L);
        r2.setName("Dominos");

        when(restaurantRepository.findAll()).thenReturn(Arrays.asList(r1,r2));
        List<Restaurant> result = restaurantService.getAllRestaurant();
        assertEquals(2, result.size());
        assertEquals("Pizza Hut", result.get(0).getName());
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void testGetRestaurantById_Success() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        Restaurant result = restaurantService.getRestaurantById(1L);
        assertNotNull(result);
        assertEquals("Test Restaurant", result.getName());
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void testGetRestaurantById_NotFound() {
        when(restaurantRepository.findById(2L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            restaurantService.getRestaurantById(2L);
        });
        assertEquals(RESTAURANT_NOT_FOUND + 2L, exception.getMessage());
        verify(restaurantRepository, times(1)).findById(2L);
    }

    @Test
    void testGetRestaurantWithMenu_Found() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("KFC");

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        Restaurant result = restaurantService.getRestaurantWithMenu(1L);
        assertNotNull(result);
        assertEquals("KFC", result.getName());
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void testGetRestaurantWithMenu_NotFound() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> restaurantService.getRestaurantWithMenu(99L));

        assertEquals( RESTAURANT_NOT_FOUND + 99, exception.getMessage());
        verify(restaurantRepository, times(1)).findById(99L);
    }

    @Test
    void testUpdateRestaurant_Success() {
        // Existing restaurant
        Restaurant existingRestaurant = new Restaurant();
        existingRestaurant.setId(1L);
        existingRestaurant.setName("Old Name");
        existingRestaurant.setAddress("Old Address");
        existingRestaurant.setContact("1111");
        // Updated request
        Restaurant updatedRestaurant = new Restaurant();
        updatedRestaurant.setName("New Name");
        updatedRestaurant.setAddress("New Address");
        updatedRestaurant.setContact("2222");
        // Add menu items
        MenuItem item1 = new MenuItem();
        item1.setId(10L);
        item1.setName("Pizza");

        updatedRestaurant.setMenuItems(List.of(item1));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(existingRestaurant));
        when(restaurantRepository.save(existingRestaurant)).thenReturn(existingRestaurant);
        Restaurant result = restaurantService.updateRestaurant(1L, updatedRestaurant);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New Address", result.getAddress());
        assertEquals("2222", result.getContact());
        assertEquals(1, result.getMenuItems().size());
        assertEquals(existingRestaurant, result.getMenuItems().get(0).getRestaurant());
        verify(restaurantRepository, times(1)).findById(1L);
        verify(restaurantRepository, times(1)).save(existingRestaurant);
    }

    @Test
    void testUpdateRestaurant_NotFound() {
        Restaurant updatedRestaurant = new Restaurant();
        updatedRestaurant.setName("New Name");

        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            restaurantService.updateRestaurant(1L, updatedRestaurant);
        });
        assertEquals(RESTAURANT_NOT_FOUND + 1L, exception.getMessage());
        verify(restaurantRepository, times(1)).findById(1L);
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void testDeleteRestaurant() {
        Long id = 1L;
        doNothing().when(restaurantRepository).deleteById(id);
        restaurantService.deleteRestaurant(id);
        verify(restaurantRepository, times(1)).deleteById(id);
    }
}
