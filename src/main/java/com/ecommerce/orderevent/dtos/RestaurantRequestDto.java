package com.ecommerce.orderevent.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RestaurantRequestDto {
    @NotBlank(message = "name is required")
    private String name;
    private String address;
    private String contact;
    private List<MenuItemRequestDto> menuItems;
}
