package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.MenuItemRequestDto;
import com.ecommerce.orderevent.dtos.RestaurantRequestDto;
import com.ecommerce.orderevent.dtos.RestaurantResponseDto;
import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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
    void testGetAllRestaurant_CacheHit() throws Exception {
        String key = "restaurant:all";
        String cachedJson = """
            [
              {"id":1,"name":"Pizza Hut","contact":"123"},
              {"id":2,"name":"Domino’s","contact":"456"}
            ]
            """;
        when(valueOps.get(key)).thenReturn(cachedJson);

        // Create DTOs manually using setters
        RestaurantResponseDto dto1 = new RestaurantResponseDto();
        dto1.setId(1L);
        dto1.setName("Pizza Hut");
        dto1.setContact("123");

        RestaurantResponseDto dto2 = new RestaurantResponseDto();
        dto2.setId(2L);
        dto2.setName("Domino’s");
        dto2.setContact("456");

        when(objectMapper.readValue(eq(cachedJson),
                ArgumentMatchers.<TypeReference<List<RestaurantResponseDto>>>any()))
                .thenReturn(List.of(dto1, dto2));

        List<RestaurantResponseDto> result = restaurantService.getAllRestaurant();
        // ✅ Assertions
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Pizza Hut", result.get(0).getName());
        assertEquals("Domino’s", result.get(1).getName());
        // ✅ Verifications
        verify(restaurantRepository, never()).findAll(); // no DB call
        verify(valueOps, times(1)).get(key); // cache hit
    }

    @Test
    void testGetAllRestaurant_DeserializationFailure() throws Exception {
        String key = "restaurant:all";
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);

        String invalidJson = "{invalid json]";
        when(valueOps.get(key)).thenReturn(invalidJson);

        // simulate deserialization failure
        when(objectMapper.readValue(eq(invalidJson), ArgumentMatchers.<TypeReference<List<RestaurantResponseDto>>>any()))
                .thenThrow(new RuntimeException("JSON parse error"));

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Fallback DB Restaurant");
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));
        when(objectMapper.writeValueAsString(any())).thenReturn("[{...}]");

        List<RestaurantResponseDto> result = restaurantService.getAllRestaurant();

        assertEquals(1, result.size());
        assertEquals("Fallback DB Restaurant", result.get(0).getName());
        verify(restaurantRepository, times(1)).findAll(); // ✅ fallback to DB
    }

    @Test
    void testGetAllRestaurant_SerializationFailure() throws Exception {
        String key = "restaurant:all";
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(key)).thenReturn(null); // no cache

        Restaurant r1 = new Restaurant();
        r1.setName("Subway");
        when(restaurantRepository.findAll()).thenReturn(List.of(r1));

        // Simulate serialization error
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Write error"));

        List<RestaurantResponseDto> result = restaurantService.getAllRestaurant();

        assertEquals(1, result.size());
        assertEquals("Subway", result.get(0).getName());
        verify(restaurantRepository, times(1)).findAll();
        verify(valueOps, never()).set(any(), any(), anyLong(), any());
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
    void testGetRestaurantById_CacheHit() throws Exception {
        Long id = 1L;
        String key = "restaurant:" + id;

        // Cached JSON stored in Redis
        String cachedJson = "{\"id\":1,\"name\":\"McDonald's\",\"contact\":\"123\"}";
        when(valueOps.get(key)).thenReturn(cachedJson);

        // Mock successful JSON deserialization
        RestaurantResponseDto cachedDto = new RestaurantResponseDto();
        cachedDto.setId(1L);
        cachedDto.setName("McDonald's");
        cachedDto.setContact("123");

        when(objectMapper.readValue(eq(cachedJson), eq(RestaurantResponseDto.class))).thenReturn(cachedDto);

        // Execute
        RestaurantResponseDto result = restaurantService.getRestaurantById(id);
        // Verify
        assertNotNull(result);
        assertEquals("McDonald's", result.getName());
        assertEquals("123", result.getContact());
        verify(restaurantRepository, never()).findById(any()); // ✅ Should NOT hit DB
        verify(valueOps, times(1)).get(key); // ✅ Reads from cache
    }

    @Test
    void testGetRestaurantById_DeserializationFailure() throws Exception {
        Long id = 5L;
        String key = "restaurant:" + id;
        String corruptedJson = "{bad json}";
        when(valueOps.get(key)).thenReturn(corruptedJson);

        // Simulate Jackson failing to parse JSON
        when(objectMapper.readValue(eq(corruptedJson), eq(RestaurantResponseDto.class)))
                .thenThrow(new RuntimeException("JSON parse error"));
        // DB fallback
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName("Burger King");
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));
        when(objectMapper.writeValueAsString(any())).thenReturn("{...}");

        // Execute
        RestaurantResponseDto dto = restaurantService.getRestaurantById(id);
        // Verify
        assertNotNull(dto);
        assertEquals("Burger King", dto.getName());
        verify(restaurantRepository, times(1)).findById(id); // ✅ Fallback used DB
        verify(valueOps, times(1)).get(key); // ✅ Attempted cache read
        verify(valueOps, times(1)).set(eq(key), anyString(), anyLong(), eq(TimeUnit.MINUTES)); // ✅ Wrote fresh cache
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
    void testUpdateRestaurant_NotFound() {
        Long id = 99L;

        // Mock repository to return empty Optional (no restaurant found)
        when(restaurantRepository.findById(id)).thenReturn(Optional.empty());

        // Prepare a dummy request DTO
        RestaurantRequestDto request = new RestaurantRequestDto();
        request.setName("Updated Name");
        request.setAddress("Updated Address");
        request.setContact("1234567890");
        request.setEmail("updated@email.com");

        // Expect exception when trying to update a non-existing restaurant
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> restaurantService.updateRestaurant(id, request));

        // ✅ Assert exception message
        assertEquals("Restaurant not found with id: " + id, exception.getMessage());
        // ✅ Verify no DB save and no cache deletion happened
        verify(restaurantRepository, times(1)).findById(id);
        verify(restaurantRepository, never()).save(any(Restaurant.class));
        verify(redisTemplate, never()).delete("restaurant:" + id);
        verify(redisTemplate, never()).delete("restaurant:all");
    }

    @Test
    void testUpdateRestaurant_WithMenuItems() {
        Long id = 10L;
        // Existing restaurant
        Restaurant existing = new Restaurant();
        existing.setId(id);
        existing.setName("Old Name");

        when(restaurantRepository.findById(id)).thenReturn(Optional.of(existing));
        // Incoming request DTO with menu items
        RestaurantRequestDto requestDto = new RestaurantRequestDto();
        requestDto.setName("Updated Restaurant");

        MenuItemRequestDto item1 = new MenuItemRequestDto();
        item1.setName("Burger");
        item1.setDescription("Cheesy Delight");
        item1.setPrice(199.0);

        MenuItemRequestDto item2 = new MenuItemRequestDto();
        item2.setName("Fries");
        item2.setDescription("Crispy Golden");
        item2.setPrice(99.0);

        requestDto.setMenuItems(List.of(item1, item2));

        // Simulated saved restaurant
        Restaurant saved = new Restaurant();
        saved.setId(id);
        saved.setName("Updated Restaurant");

        MenuItem m1 = new MenuItem();
        m1.setId(1L);
        m1.setName("Burger");
        m1.setDescription("Cheesy Delight");
        m1.setPrice(199.0);
        m1.setRestaurant(saved);

        MenuItem m2 = new MenuItem();
        m2.setId(2L);
        m2.setName("Fries");
        m2.setDescription("Crispy Golden");
        m2.setPrice(99.0);
        m2.setRestaurant(saved);

        saved.setMenuItems(List.of(m1, m2));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(saved);
        // Act
        RestaurantResponseDto result = restaurantService.updateRestaurant(id, requestDto);
        // Assert
        assertEquals("Updated Restaurant", result.getName());
        assertEquals(2, result.getMenuItems().size());
        assertEquals("Burger", result.getMenuItems().get(0).getName());
        assertEquals(199.0, result.getMenuItems().get(0).getPrice());
        assertEquals("Fries", result.getMenuItems().get(1).getName());
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