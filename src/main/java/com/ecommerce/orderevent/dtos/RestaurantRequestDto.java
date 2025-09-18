package com.ecommerce.orderevent.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RestaurantRequestDto {
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "email is required")
    private String email;
    private String address;
    private String contact;
    private List<MenuItemRequestDto> menuItems;
}
