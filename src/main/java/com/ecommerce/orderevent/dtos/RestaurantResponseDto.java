package com.ecommerce.orderevent.dtos;

import com.ecommerce.orderevent.entity.Restaurant;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RestaurantResponseDto {
    private Long id;
    private String name;
    private String address;
    private String contact;
    private List<MenuItemResponseDto> menuItems;

    public static RestaurantResponseDto fromEntity(Restaurant restaurant) {
        RestaurantResponseDto dto = new RestaurantResponseDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setContact(restaurant.getContact());

        if (restaurant.getMenuItems() != null) {
            dto.setMenuItems(
                    restaurant.getMenuItems().stream()
                            .map(MenuItemResponseDto::fromEntity) // map each MenuItem -> DTO
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }
}
