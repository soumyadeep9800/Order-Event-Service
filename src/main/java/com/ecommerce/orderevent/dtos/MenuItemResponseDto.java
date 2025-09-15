package com.ecommerce.orderevent.dtos;

import com.ecommerce.orderevent.entity.MenuItem;
import lombok.Data;

@Data
public class MenuItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private RestaurantResponseDto restaurant;

    public static MenuItemResponseDto fromEntity(MenuItem menuItem) {
        MenuItemResponseDto dto = new MenuItemResponseDto();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());

        RestaurantResponseDto restaurantDto = new RestaurantResponseDto();
        restaurantDto.setId(menuItem.getRestaurant().getId());
        restaurantDto.setName(menuItem.getRestaurant().getName());
        restaurantDto.setAddress(menuItem.getRestaurant().getAddress());
        restaurantDto.setContact(menuItem.getRestaurant().getContact());
        dto.setRestaurant(restaurantDto);

        return dto;
    }
}
