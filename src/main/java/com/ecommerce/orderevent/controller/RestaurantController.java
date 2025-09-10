package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.service.MenuItemService;
import com.ecommerce.orderevent.service.RestaurantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/restaurants")
@Tag(name = "Restaurant Management", description = "Endpoints for managing restaurants")
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

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id){
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/{id}/menu")
    public List<MenuItem> menuOfRestaurant(@PathVariable Long id){
        return menuItemService.getMenuItemsByRestaurant(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable Long id, @RequestBody Restaurant updateRestaurant){
        Restaurant restaurant = restaurantService.updateRestaurant(id, updateRestaurant);
        return ResponseEntity.ok(restaurant);
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

}
