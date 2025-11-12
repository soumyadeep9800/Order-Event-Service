package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.RestaurantRequestDto;
import com.ecommerce.orderevent.dtos.RestaurantResponseDto;
import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static com.ecommerce.orderevent.constants.ErrorMessages.RESTAURANT_NOT_FOUND;

@Service
@Slf4j
public class RestaurantService {
    private static final String RESTAURANT_CACHE_PREFIX = "restaurant:";
    private static final long RESTAURANT_CACHE_TTL = 10;

    private final RestaurantRepository restaurantRepository;
    private final RedisTemplate<String,Object> redisTemplate;
    public RestaurantService(RestaurantRepository restaurantRepository,RedisTemplate<String, Object> redisTemplate){
        this.restaurantRepository = restaurantRepository;
        this.redisTemplate=redisTemplate;
    }

    public RestaurantResponseDto addRestaurant(RestaurantRequestDto restaurantRequestDto){
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantRequestDto.getName());
        restaurant.setContact(restaurantRequestDto.getContact());
        restaurant.setAddress(restaurantRequestDto.getAddress());
        restaurant.setEmail(restaurantRequestDto.getEmail());

        if (restaurantRequestDto.getMenuItems() != null) {
            List<MenuItem> menuItems = restaurantRequestDto.getMenuItems().stream()
                    .map(itemDto -> {
                        MenuItem item = new MenuItem();
                        item.setName(itemDto.getName());
                        item.setDescription(itemDto.getDescription());
                        item.setPrice(itemDto.getPrice());
                        item.setRestaurant(restaurant); // set relationship
                        return item;
                    })
                    .toList();
            restaurant.setMenuItems(menuItems);
        }
        Restaurant saveRestaurant = restaurantRepository.save(restaurant);
        redisTemplate.delete(RESTAURANT_CACHE_PREFIX + "all");
        log.info("🍽️ Added restaurant: {}", saveRestaurant.getName());
        return RestaurantResponseDto.fromEntity(saveRestaurant);
    }

    public RestaurantResponseDto updateRestaurant(Long id, RestaurantRequestDto restaurantRequestDto) {
        Restaurant existingRestaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ id));

        // Update allowed fields
        existingRestaurant.setName(restaurantRequestDto.getName());
        existingRestaurant.setAddress(restaurantRequestDto.getAddress());
        existingRestaurant.setContact(restaurantRequestDto.getContact());
        existingRestaurant.setEmail(restaurantRequestDto.getEmail());

        if (restaurantRequestDto.getMenuItems() != null) {
            List<MenuItem> menuItems = restaurantRequestDto.getMenuItems().stream()
                    .map(itemDto -> {
                        MenuItem item = new MenuItem();
                        item.setName(itemDto.getName());
                        item.setDescription(itemDto.getDescription());
                        item.setPrice(itemDto.getPrice());
                        item.setRestaurant(existingRestaurant); // set relationship
                        return item;
                    })
                    .toList();
            existingRestaurant.setMenuItems(menuItems);
        }
        Restaurant restaurant = restaurantRepository.save(existingRestaurant);

        // Invalidate the old cache
        String key = RESTAURANT_CACHE_PREFIX + id;
        redisTemplate.delete(key);
        redisTemplate.delete(RESTAURANT_CACHE_PREFIX + "all");
        log.info("♻️ Cache invalidated for restaurant {}", id);

        return RestaurantResponseDto.fromEntity(restaurant);
    }

    public List<Restaurant> getAllRestaurant(){
        String key = RESTAURANT_CACHE_PREFIX + "all";

        List<Restaurant> cachedList = (List<Restaurant>) redisTemplate.opsForValue().get(key);
        if (cachedList != null) {
            log.info("✅ Fetched restaurant list from Redis Cache");
            return cachedList;
        }

        List<Restaurant> restaurants = restaurantRepository.findAll();
        redisTemplate.opsForValue().set(key, restaurants, 5, TimeUnit.MINUTES);
        log.info("💾 Saved restaurant list to Redis Cache");
        return restaurants;
    }

    public Restaurant getRestaurantById(Long id){
        String key = RESTAURANT_CACHE_PREFIX + id;

        // 1️⃣ Check if restaurant exists in Redis
        Restaurant restaurant = (Restaurant) redisTemplate.opsForValue().get(key);
        if (restaurant != null) {
            log.info("✅ Fetched from Redis Cache");
            return restaurant;
        }

        // 2️⃣ Otherwise, fetch from DB
        restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND + id));

        // 3️⃣ Save to Redis cache for 10 minutes
        redisTemplate.opsForValue().set(key, restaurant, RESTAURANT_CACHE_TTL, TimeUnit.MINUTES);

        log.info("💾 Saved to Redis Cache");
        return restaurant;
    }

    public Restaurant getRestaurantWithMenu(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ id));
    }

    public void deleteRestaurant(Long id){
        if(!restaurantRepository.existsById(id)) throw new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ id);
        restaurantRepository.deleteById(id);
        redisTemplate.delete(RESTAURANT_CACHE_PREFIX + id);
        redisTemplate.delete(RESTAURANT_CACHE_PREFIX + "all");
        log.info("🗑️ Deleted from Redis cache as well: restaurant {}", id);
    }
}
