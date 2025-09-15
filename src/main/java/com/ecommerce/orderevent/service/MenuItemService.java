package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.dtos.MenuItemRequestDto;
import com.ecommerce.orderevent.dtos.MenuItemResponseDto;
import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.exception.ResourceNotFoundException;
import com.ecommerce.orderevent.repository.MenuItemRepository;
import com.ecommerce.orderevent.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.ecommerce.orderevent.constants.ErrorMessages.MENU_ITEM_NOT_FOUND;
import static com.ecommerce.orderevent.constants.ErrorMessages.RESTAURANT_NOT_FOUND;

@Service
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemService(MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository){
        this.menuItemRepository=menuItemRepository;
        this.restaurantRepository=restaurantRepository;
    }

    public MenuItemResponseDto addMenuItem(Long restaurantId, MenuItemRequestDto menuItemRequestDto){
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException( RESTAURANT_NOT_FOUND + restaurantId));

        MenuItem menuItem = new MenuItem();
        menuItem.setPrice(menuItemRequestDto.getPrice());
        menuItem.setName(menuItemRequestDto.getName());
        menuItem.setDescription(menuItemRequestDto.getDescription());
        menuItem.setRestaurant(restaurant);

        MenuItem saveMenuItem = menuItemRepository.save(menuItem);
        return MenuItemResponseDto.fromEntity(saveMenuItem);
    }

    public MenuItemResponseDto updateMenuItem(Long id, MenuItemRequestDto menuItemRequestDto){
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(MENU_ITEM_NOT_FOUND + id));

        menuItem.setName(menuItemRequestDto.getName());
        menuItem.setDescription(menuItemRequestDto.getDescription());
        menuItem.setPrice(menuItemRequestDto.getPrice());
        if (menuItemRequestDto.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(menuItemRequestDto.getRestaurantId())
                    .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND + menuItemRequestDto.getRestaurantId()));
            menuItem.setRestaurant(restaurant);
        }

        MenuItem saveMenuItem = menuItemRepository.save(menuItem);
        return MenuItemResponseDto.fromEntity(saveMenuItem);
    }

    public List<MenuItem> getMenuItemsByRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() ->  new ResourceNotFoundException( RESTAURANT_NOT_FOUND + restaurantId));
        return restaurant.getMenuItems();
    }

    public MenuItem findMenuItem(Long menuId) {
        return menuItemRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(MENU_ITEM_NOT_FOUND + menuId));
    }

    public void deleteMenuItem(Long id){
        if(!menuItemRepository.existsById(id)) throw new ResourceNotFoundException(MENU_ITEM_NOT_FOUND + id);
        menuItemRepository.deleteById(id);
    }
}
