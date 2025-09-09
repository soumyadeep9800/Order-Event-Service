package com.ecommerce.orderevent.service;

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

    public Restaurant addRestaurant(Restaurant restaurant){
        if(restaurant.getMenuItems() != null){
            restaurant.getMenuItems().forEach(item -> item.setRestaurant(restaurant));
        }
        return restaurantRepository.save(restaurant);
    }

    public List<Restaurant> getAllRestaurant(){
        return restaurantRepository.findAll();
    }

    public Restaurant getRestaurantById(Long id){
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ id));
    }

    public Restaurant getRestaurantWithMenu(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ id));
    }

    public Restaurant updateRestaurant(Long id, Restaurant updatedRestaurant) {
        Restaurant existingRestaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND+ id));

        // Update allowed fields
        existingRestaurant.setName(updatedRestaurant.getName());
        existingRestaurant.setAddress(updatedRestaurant.getAddress());
        existingRestaurant.setContact(updatedRestaurant.getContact());

        if (updatedRestaurant.getMenuItems() != null) {
            updatedRestaurant.getMenuItems().forEach(item -> item.setRestaurant(existingRestaurant));
            existingRestaurant.setMenuItems(updatedRestaurant.getMenuItems());
        }
        return restaurantRepository.save(existingRestaurant);
    }

    public void deleteRestaurant(Long id){
        restaurantRepository.deleteById(id);
    }
}
