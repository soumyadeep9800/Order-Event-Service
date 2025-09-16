package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dtos.ApiResponse;
import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;

import com.ecommerce.orderevent.dtos.RestaurantRequestDto;
import com.ecommerce.orderevent.dtos.RestaurantResponseDto;
import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.entity.Restaurant;
import com.ecommerce.orderevent.service.MenuItemService;
import com.ecommerce.orderevent.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
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
    @Operation(summary = "Add new restaurant", description = "Create a new restaurant and save it to the database")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> addNewRestaurant(@RequestBody RestaurantRequestDto restaurantRequestDto){
        RestaurantResponseDto saveRestaurant = restaurantService.addRestaurant(restaurantRequestDto);
        ApiResponse<RestaurantResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "Restaurant added successfully!",
                saveRestaurant,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update restaurant", description = "Update details of an existing restaurant by its ID")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> updateRestaurant(@PathVariable Long id, @RequestBody RestaurantRequestDto restaurantRequestDto){
        RestaurantResponseDto restaurant = restaurantService.updateRestaurant(id, restaurantRequestDto);
        ApiResponse<RestaurantResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "Update restaurant successfully!",
                restaurant,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    @Operation(summary = "Fetch all restaurants", description = "Retrieve a list of all restaurants")
    public ResponseEntity<ApiResponse<List<Restaurant>>> getAllRestaurant(){
        List<Restaurant> allRestaurant = restaurantService.getAllRestaurant();
        ApiResponse<List<Restaurant>> response = new ApiResponse<>(
                SUCCESS,
                "All restaurants fetched successfully!",
                allRestaurant,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch restaurant by ID", description = "Get details of a specific restaurant by its ID")
    public ResponseEntity<ApiResponse<Restaurant>> getRestaurantById(@PathVariable Long id){
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        ApiResponse<Restaurant> response = new ApiResponse<>(
                SUCCESS,
                "Restaurant details fetched successfully!",
                restaurant,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/menu")
    @Operation(summary = "Fetch menu of a restaurant", description = "Retrieve all menu items for a specific restaurant by its ID")
    public ResponseEntity<ApiResponse<List<MenuItem>>> menuOfRestaurant(@PathVariable Long id){
        List<MenuItem> listOfMenus = menuItemService.getMenuItemsByRestaurant(id);
        ApiResponse<List<MenuItem>> response = new ApiResponse<>(
                SUCCESS,
                "Menu-items of the restaurant fetched successfully!",
                listOfMenus,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete restaurant", description = "Delete an existing restaurant by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        ApiResponse<Void> response = new ApiResponse<>(
                SUCCESS,
                "Restaurant deleted successfully!",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

}
