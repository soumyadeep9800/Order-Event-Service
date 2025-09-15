package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dtos.ApiResponse;
import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;
import com.ecommerce.orderevent.dtos.MenuItemRequestDto;
import com.ecommerce.orderevent.dtos.MenuItemResponseDto;
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
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> addMenuItem(@RequestBody MenuItemRequestDto menuItemRequestDto){
        Long id = menuItemRequestDto.getRestaurantId();
        MenuItemResponseDto getMenuitem = menuItemService.addMenuItem(id,menuItemRequestDto);
        ApiResponse<MenuItemResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "Menu-item saved successfully!",
                getMenuitem,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> updateMenuItem(@PathVariable Long id, @RequestBody MenuItemRequestDto menuItemRequestDto){
        MenuItemResponseDto updatedMenu = menuItemService.updateMenuItem(id, menuItemRequestDto);
        ApiResponse<MenuItemResponseDto> response = new ApiResponse<>(
                SUCCESS,
                "Menu-item updated successfully!",
                updatedMenu,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
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
