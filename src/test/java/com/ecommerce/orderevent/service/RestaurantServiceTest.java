package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
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
    void testDeleteRestaurant() {
        Long id = 1L;
        doNothing().when(restaurantRepository).deleteById(id);
        restaurantService.deleteRestaurant(id);
        verify(restaurantRepository, times(1)).deleteById(id);
    }
}
