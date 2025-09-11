package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dto.ApiResponse;
import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;
import com.ecommerce.orderevent.entity.MenuItem;
import com.ecommerce.orderevent.service.MenuItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/menu-item")
@Tag(name = "Menu Item Management", description = "Endpoints for managing menu items")
public class MenuItemController {
    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService){
        this.menuItemService=menuItemService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItem>> addMenuItem(@RequestBody MenuItem menuItem){
        Long id = menuItem.getRestaurant().getId();
        MenuItem getMenuitem = menuItemService.addMenuItem(id,menuItem);
        ApiResponse<MenuItem> response = new ApiResponse<>(
                SUCCESS,
                "Menu-item saved successfully!",
                getMenuitem,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/menu-items/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> getMenuItem(@PathVariable Long id) {
        MenuItem menuItem = menuItemService.findMenuItem(id);
        ApiResponse<MenuItem> response = new ApiResponse<>(
                SUCCESS,
                "Menu-item fetched successfully!",
                menuItem,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItem){
        MenuItem updatedMenu = menuItemService.updateMenuItem(id, menuItem);
        ApiResponse<MenuItem> response = new ApiResponse<>(
                SUCCESS,
                "Menu-item updated successfully!",
                updatedMenu,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id){
        menuItemService.deleteMenuItem(id);
        ApiResponse<Void> response = new ApiResponse<>(
                SUCCESS,
                "Menu-item deleted successfully!",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
