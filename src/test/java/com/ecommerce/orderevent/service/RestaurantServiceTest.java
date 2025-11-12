package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.MenuItemRequestDto;
import com.ecommerce.orderevent.dtos.RestaurantRequestDto;
import com.ecommerce.orderevent.dtos.RestaurantResponseDto;
import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RestaurantService restaurantService;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void testAddRestaurant_Success() {
        RestaurantRequestDto requestDto = new RestaurantRequestDto();
        requestDto.setName("Pizza Hut");
        requestDto.setContact("1234567890");
        requestDto.setAddress("Main Street");

        MenuItemRequestDto itemDto = new MenuItemRequestDto();
        itemDto.setName("Chicken Pizza");
        itemDto.setDescription("Spicy chicken pizza");
        itemDto.setPrice(299.0);
        requestDto.setMenuItems(List.of(itemDto));

        Restaurant savedEntity = new Restaurant();
        savedEntity.setId(1L);
        savedEntity.setName("Pizza Hut");

        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setName("Chicken Pizza");
        item.setPrice(299.0);
        item.setRestaurant(savedEntity);
        savedEntity.setMenuItems(List.of(item));

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(savedEntity);

        RestaurantResponseDto result = restaurantService.addRestaurant(requestDto);

        assertNotNull(result);
        assertEquals("Pizza Hut", result.getName());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        verify(redisTemplate, times(1)).delete("restaurant:all");
    }

    @Test
    void testGetAllRestaurant_CacheMiss() throws Exception {
        String key = "restaurant:all";
        when(valueOps.get(key)).thenReturn(null);

        Restaurant r1 = new Restaurant();
        r1.setName("Pizza Hut");
        Restaurant r2 = new Restaurant();
        r2.setName("Domino’s");

        when(restaurantRepository.findAll()).thenReturn(List.of(r1, r2));
        when(objectMapper.writeValueAsString(any())).thenReturn("[{...}]");

        List<RestaurantResponseDto> result = restaurantService.getAllRestaurant();

        assertEquals(2, result.size());
        assertEquals("Pizza Hut", result.get(0).getName());
        verify(valueOps, times(1)).set(eq(key), anyString(), anyLong(), eq(TimeUnit.MINUTES));
    }

    @Test
    void testGetRestaurantById_SuccessFromDB() throws Exception {
        Long id = 1L;
        String key = "restaurant:" + id;

        when(valueOps.get(key)).thenReturn(null);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName("KFC");
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));
        when(objectMapper.writeValueAsString(any())).thenReturn("{...}");

        RestaurantResponseDto dto = restaurantService.getRestaurantById(id);

        assertEquals("KFC", dto.getName());
        verify(valueOps, times(1)).set(eq(key), anyString(), anyLong(), eq(TimeUnit.MINUTES));
    }

    @Test
    void testGetRestaurantById_NotFound() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> restaurantService.getRestaurantById(99L));
        verify(restaurantRepository, times(1)).findById(99L);
    }

    @Test
    void testUpdateRestaurant_Success() {
        Long id = 1L;
        Restaurant existing = new Restaurant();
        existing.setId(id);
        existing.setName("Old Restaurant");
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(existing));

        RestaurantRequestDto request = new RestaurantRequestDto();
        request.setName("New Restaurant");

        Restaurant updated = new Restaurant();
        updated.setId(id);
        updated.setName("New Restaurant");

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(updated);

        RestaurantResponseDto result = restaurantService.updateRestaurant(id, request);

        assertEquals("New Restaurant", result.getName());
        verify(redisTemplate, times(1)).delete("restaurant:" + id);
        verify(redisTemplate, times(1)).delete("restaurant:all");
    }

    @Test
    void testDeleteRestaurant_Success() {
        when(restaurantRepository.existsById(1L)).thenReturn(true);
        doNothing().when(restaurantRepository).deleteById(1L);
        restaurantService.deleteRestaurant(1L);
        verify(redisTemplate).delete("restaurant:1");
        verify(redisTemplate).delete("restaurant:all");
    }

    @Test
    void testDeleteRestaurant_NotFound() {
        when(restaurantRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> restaurantService.deleteRestaurant(1L));
    }
}