package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.RestaurantRequestDto;
import com.ecommerce.orderevent.dtos.RestaurantResponseDto;
import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.ecommerce.orderevent.constants.ErrorMessages.RESTAURANT_NOT_FOUND;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    public RestaurantService(RestaurantRepository restaurantRepository){
        this.restaurantRepository = restaurantRepository;
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
        return RestaurantResponseDto.fromEntity(restaurant);
    }

    public List<Restaurant> getAllRestaurant(){ return restaurantRepository.findAll(); }

    public Restaurant getRestaurantById(Long id){
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ id));
    }

    public Restaurant getRestaurantWithMenu(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ id));
    }

    public void deleteRestaurant(Long id){
        if(!restaurantRepository.existsById(id)) throw new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ id);
        restaurantRepository.deleteById(id);
    }
}
