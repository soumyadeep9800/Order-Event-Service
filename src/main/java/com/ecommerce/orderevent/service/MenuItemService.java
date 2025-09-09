package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.MenuItemRepository;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import java.util.*;

import static com.ecommerce.orderevent.constants.ErrorMessages.RESTAURANT_NOT_FOUND;

@Service
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemService(MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository){
        this.menuItemRepository=menuItemRepository;
        this.restaurantRepository=restaurantRepository;
    }

    public MenuItem addMenuItem(Long restaurantId, MenuItem menuItem){
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException( RESTAURANT_NOT_FOUND + restaurantId));

        menuItem.setRestaurant(restaurant);
        return menuItemRepository.save(menuItem);
    }

    public List<MenuItem> getMenuItemsByRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() ->  new ResourceNotFoundException( RESTAURANT_NOT_FOUND + restaurantId));
        return restaurant.getMenuItems();
    }

    public Optional<MenuItem> findMenuItem(Long menuId){
        return menuItemRepository.findById(menuId);
    }

    public void deleteMenuItem(Long id){
        menuItemRepository.deleteById(id);
    }
}
