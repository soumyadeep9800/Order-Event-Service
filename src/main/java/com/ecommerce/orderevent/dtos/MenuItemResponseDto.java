package com.ecommerce.orderevent.dtos;

import com.ecommerce.orderevent.entity.MenuItem;
import lombok.Data;

@Data
public class MenuItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String restaurantName;

    public static MenuItemResponseDto fromEntity(MenuItem menuItem) {
        MenuItemResponseDto dto = new MenuItemResponseDto();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        if (menuItem.getRestaurant() != null) {
            dto.setRestaurantName(menuItem.getRestaurant().getName());
        }
        return dto;
    }
}
