package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.service.MenuItemService;
import com.ecommerce.orderevent.service.RestaurantService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    public RestaurantController(RestaurantService restaurantService, MenuItemService menuItemService){
        this.restaurantService=restaurantService;
        this.menuItemService=menuItemService;
    }

    @PostMapping
    public Restaurant addNewRestaurant(@RequestBody Restaurant restaurant){
        return restaurantService.addRestaurant(restaurant);
    }

    @GetMapping
    public List<Restaurant> getAllRestaurant(){
        return restaurantService.getAllRestaurant();
    }

    @DeleteMapping("/{id}")
    public String deleteRestaurant(@PathVariable Long id){
        try{
            restaurantService.deleteRestaurant(id);
            return "Restaurant Delete Successfully!";
        } catch (Exception e) {
            return " Restaurant can not deleted "+ e;
        }
    }

    @GetMapping("/{id}/menu")
    public List<MenuItem> menuOfRestaurant(@PathVariable Long id){
        return menuItemService.getMenuItemsByRestaurant(id);
    }
}
