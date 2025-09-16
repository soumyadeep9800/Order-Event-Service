package com.ecommerce.orderevent.dtos;

import com.ecommerce.orderevent.entity.Order;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderResponseDto {
    private Long id;
    private LocalDateTime orderDate;
    private String status;
    private Double totalPrice;

    private UserResponseDto user;
    private RestaurantResponseDto restaurant;
    private List<MenuItemResponseDto> items;

    public static OrderResponseDto fromEntity(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());

        // Map user → lightweight DTO
        if (order.getUser() != null) {
            UserResponseDto userDto = new UserResponseDto();
            userDto.setId(order.getUser().getId());
            userDto.setName(order.getUser().getName());
            userDto.setEmail(order.getUser().getEmail());
            dto.setUser(userDto);
        }

        // Map restaurant
        if (order.getRestaurant() != null) {
            RestaurantResponseDto restaurantDto = new RestaurantResponseDto();
            restaurantDto.setId(order.getRestaurant().getId());
            restaurantDto.setName(order.getRestaurant().getName());
            restaurantDto.setAddress(order.getRestaurant().getAddress());
            restaurantDto.setContact(order.getRestaurant().getContact());
            dto.setRestaurant(restaurantDto);
        }

        // Map menu items
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(MenuItemResponseDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
