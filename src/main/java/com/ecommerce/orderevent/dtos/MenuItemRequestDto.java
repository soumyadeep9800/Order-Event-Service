package com.ecommerce.orderevent.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuItemRequestDto {
    @NotBlank(message = "name is required")
    private String name;
    private String description;
    private Double price;
    private Long restaurantId;
}
