package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.MenuItemRequestDto;
import com.ecommerce.orderevent.dtos.RestaurantRequestDto;
import com.ecommerce.orderevent.dtos.RestaurantResponseDto;
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
    void TestAddRestaurant_Success() {
        // Arrange (prepare input DTO)
        RestaurantRequestDto requestDto = new RestaurantRequestDto();
        requestDto.setName("Pizza Hut");
        requestDto.setContact("1234567890");
        requestDto.setAddress("Main Street");

        MenuItemRequestDto itemDto = new MenuItemRequestDto();
        itemDto.setName("Chicken Pizza");
        itemDto.setDescription("Spicy chicken pizza");
        itemDto.setPrice(299.0);
        requestDto.setMenuItems(Arrays.asList(itemDto));

        // Mock entity to return
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Pizza Hut");

        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setName("Chicken Pizza");
        item.setPrice(299.0);
        item.setRestaurant(restaurant);
        restaurant.setMenuItems(Arrays.asList(item));

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);
        // Act
        RestaurantResponseDto saved = restaurantService.addRestaurant(requestDto);
        // Assert
        assertNotNull(saved);
        assertEquals("Pizza Hut", saved.getName());
        assertEquals("Chicken Pizza", saved.getMenuItems().get(0).getName());
        assertEquals(299.0, saved.getMenuItems().get(0).getPrice());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
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
        // Arrange: existing restaurant in DB
        Restaurant existingRestaurant = new Restaurant();
        existingRestaurant.setId(1L);
        existingRestaurant.setName("Old Name");
        existingRestaurant.setAddress("Old Address");
        existingRestaurant.setContact("1111111111");

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(existingRestaurant));

        // DTO with new values
        RestaurantRequestDto requestDto = new RestaurantRequestDto();
        requestDto.setName("New Name");
        requestDto.setAddress("New Address");
        requestDto.setContact("9999999999");

        MenuItemRequestDto menuItemRequestDto = new MenuItemRequestDto();
        menuItemRequestDto.setName("Updated Pizza");
        menuItemRequestDto.setDescription("Cheese Burst");
        menuItemRequestDto.setPrice(299.0);
        requestDto.setMenuItems(Arrays.asList(menuItemRequestDto));

        // Mock saved restaurant
        Restaurant updatedRestaurant = new Restaurant();
        updatedRestaurant.setId(1L);
        updatedRestaurant.setName("New Name");
        updatedRestaurant.setAddress("New Address");
        updatedRestaurant.setContact("9999999999");

        MenuItem menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setName("Updated Pizza");
        menuItem.setDescription("Cheese Burst");
        menuItem.setPrice(299.0);
        menuItem.setRestaurant(updatedRestaurant);
        updatedRestaurant.setMenuItems(Arrays.asList(menuItem));

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(updatedRestaurant);
        // Act
        RestaurantResponseDto response = restaurantService.updateRestaurant(1L, requestDto);
        // Assert
        assertNotNull(response);
        assertEquals("New Name", response.getName());
        assertEquals("Updated Pizza", response.getMenuItems().get(0).getName());
        assertEquals(299.0, response.getMenuItems().get(0).getPrice());
        verify(restaurantRepository, times(1)).findById(1L);
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void testUpdateRestaurant_NotFound() {
        // Arrange: restaurant not found
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());
        RestaurantRequestDto requestDto = new RestaurantRequestDto();
        requestDto.setName("Doesn't Matter");
        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            restaurantService.updateRestaurant(99L, requestDto);
        });
        verify(restaurantRepository, times(1)).findById(99L);
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }


    @Test
    void testDeleteRestaurant_Success() {
        Long id = 1L;
        when(restaurantRepository.existsById(id)).thenReturn(true);
        doNothing().when(restaurantRepository).deleteById(id);
        restaurantService.deleteRestaurant(id);
        verify(restaurantRepository, times(1)).existsById(id);
        verify(restaurantRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteRestaurant_NotFound() {
        Long id = 1L;
        when(restaurantRepository.existsById(id)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class,
                () -> restaurantService.deleteRestaurant(id));
        verify(restaurantRepository, times(1)).existsById(id);
        verify(restaurantRepository, never()).deleteById(id);
    }
}
