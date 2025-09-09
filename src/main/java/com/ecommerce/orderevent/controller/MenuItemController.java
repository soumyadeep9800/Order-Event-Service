package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.service.MenuItemService;
import com.ecommerce.orderevent.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu-item")
public class MenuItemController {
    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    public MenuItemController(RestaurantService restaurantService, MenuItemService menuItemService){
        this.restaurantService=restaurantService;
        this.menuItemService=menuItemService;
    }

    @PostMapping
    public MenuItem addMenuItem(@RequestBody MenuItem menuItem){
        Long id = menuItem.getRestaurant().getId();
        return menuItemService.addMenuItem(id,menuItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuItem(@PathVariable Long id) {
        return menuItemService.findMenuItem(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("{id}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItem){

            MenuItem updatedMenu = menuItemService.updateMenuItem(id, menuItem);
            return ResponseEntity.ok(updatedMenu);

    }

    @DeleteMapping("/{id}")
    public void deleteMenuItem(@PathVariable Long id){
        menuItemService.deleteMenuItem(id);
    }
}
